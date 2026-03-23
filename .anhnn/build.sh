#!/bin/bash

# ─────────────────────────────────────────────
# Cấu hình
# ─────────────────────────────────────────────
HOST="https://production.anhnn.com"
FILE_SERVICE="$HOST/file/service"
DISCORD_WEBHOOK_TESTING="https://discord.com/api/webhooks/1485480399210549328/9OkimO32EOZqOW7W290QZwzT807KtixzxjksOsoFot4-QUCl_Kzc2YLvQEqSzZEU2_oa"
DISCORD_WEBHOOK_PRODUCTION="https://discord.com/api/webhooks/1485480399210549328/9OkimO32EOZqOW7W290QZwzT807KtixzxjksOsoFot4-QUCl_Kzc2YLvQEqSzZEU2_oa"

BUILD_FILE="app/build.gradle.kts"
current_branch="${BRANCH_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
start_time=$(date +%s)

echo "================================================"
echo " Branch: $current_branch"
echo " Build file: $BUILD_FILE"
echo "================================================"

# ─────────────────────────────────────────────
# Bỏ qua nếu không phải nhánh chính
# ─────────────────────────────────────────────
if [ "$current_branch" != "develop" ] && \
   [ "$current_branch" != "testing" ] && \
   [ "$current_branch" != "release" ] && \
   [ "$current_branch" != "main" ]; then
    echo "Nhánh '$current_branch' không cần build tự động. Bỏ qua."
    exit 0
fi

# ─────────────────────────────────────────────
# Lấy tag hiện tại từ remote
# ─────────────────────────────────────────────
get_current_tag() {
    git fetch origin --tags 2>/dev/null || true
    latest_tag=$(git ls-remote --tags origin | awk -F'/' '{print $NF}' | grep -v '\^{}' | sort -V | tail -1)
    echo "DEBUG: git ls-remote latest_tag: $latest_tag" >&2
    if [ -z "$latest_tag" ]; then
        latest_tag="1.0.0.0"
    fi
    echo "$latest_tag"
}

# ─────────────────────────────────────────────
# Tăng version theo nhánh
# ─────────────────────────────────────────────
increase_tag() {
    local branch_name="$1"
    local current_tag
    current_tag=$(get_current_tag)
    local version
    version=$(echo "$current_tag" | sed 's/^v//' | tr -d '\r')

    if [ "$branch_name" == "develop" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$4++; print}')
    elif [ "$branch_name" == "testing" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$3++; $4=0; print}')
    elif [ "$branch_name" == "release" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$2++; $3=0; $4=0; print}')
    elif [ "$branch_name" == "main" ]; then
        version=$(echo "$version" | awk -F. -v OFS=. '{$1++; $2=0; $3=0; $4=0; print}')
    fi

    version=$(echo "$version" | tr -d '\r\n')
    echo "$version"
}

# ─────────────────────────────────────────────
# Cập nhật versionName và versionCode trong build.gradle.kts
# ─────────────────────────────────────────────
update_version_in_gradle() {
    local new_tag="$1"
    local new_code="$2"

    echo "Cập nhật $BUILD_FILE → versionName=$new_tag, versionCode=$new_code"

    # versionName = "x.x"  →  versionName = "new_tag"
    sed -i '' "s/versionName = \"[^\"]*\"/versionName = \"$new_tag\"/" "$BUILD_FILE"

    # versionCode = N  →  versionCode = new_code
    sed -i '' "s/versionCode = [0-9]*/versionCode = $new_code/" "$BUILD_FILE"

    echo "Sau khi cập nhật:"
    grep -E 'versionName|versionCode' "$BUILD_FILE"
}

# ─────────────────────────────────────────────
# Upload file lên server
# ─────────────────────────────────────────────
upload_file() {
    local file_path="$1"
    local file_name="$2"
    curl -sS -F "file=@\"$file_path\";filename=\"$file_name\"" "$FILE_SERVICE/upload"
}

# ─────────────────────────────────────────────
# Build theo nhánh
# ─────────────────────────────────────────────
run_builder() {
    echo "sdk.dir=$ANDROID_HOME" > local.properties
    echo "local.properties:"
    cat local.properties

    echo "--- Working directory ---"
    pwd
    ls -la

    rm -rf app/build/
    chmod +x ./gradlew

    export GRADLE_OPTS="-Dorg.gradle.daemon=false"

    if [ "$current_branch" == "develop" ] || [ "$current_branch" == "testing" ]; then
        echo "--- assembleDebug ---"
        ./gradlew assembleDebug --no-daemon --stacktrace 2>&1 | tee /tmp/gradle_build.log
        GRADLE_EXIT=${PIPESTATUS[0]}
        echo "Gradle exit code: $GRADLE_EXIT"
        echo "--- Tail gradle log ---"
        tail -50 /tmp/gradle_build.log
        if [ $GRADLE_EXIT -ne 0 ]; then
            echo "Gradle assembleDebug thất bại!"
            return 1
        fi
        find app/build -type f -name "*-release*" -delete 2>/dev/null || true

    elif [ "$current_branch" == "release" ] || [ "$current_branch" == "main" ]; then
        echo "--- assembleRelease ---"
        ./gradlew assembleRelease --no-daemon --stacktrace 2>&1
        GRADLE_EXIT=$?
        echo "Gradle exit code: $GRADLE_EXIT"
        if [ $GRADLE_EXIT -ne 0 ]; then
            echo "Gradle assembleRelease thất bại!"
            return 1
        fi
        echo "--- bundleRelease ---"
        ./gradlew :app:bundleRelease --no-daemon --stacktrace 2>&1
        find app/build -type f -name "*-debug*" -delete 2>/dev/null || true
    fi

    echo "--- Tìm APK toàn bộ project ---"
    find . -name "*.apk" -o -name "*.aab" 2>/dev/null | grep -v ".gradle" || true
}

# ─────────────────────────────────────────────
# Tạo git tag và push
# ─────────────────────────────────────────────
auto_create_tag() {
    echo "DEBUG: Bắt đầu auto_create_tag với newTag=$newTag"
    git config user.email "jenkins@ci.local"
    git config user.name "Jenkins CI"
    git fetch origin --tags
    # Bỏ qua nếu tag đã tồn tại
    if git rev-parse "$newTag" >/dev/null 2>&1; then
        echo "Tag $newTag đã tồn tại, bỏ qua."
        return 0
    fi
    git tag -a "$newTag" -m "[$current_branch] Auto create tag $newTag"
    git push origin "$newTag"
    echo "Tag đã push: $newTag"
}

# ─────────────────────────────────────────────
# Gửi thông báo Discord với embed + QR
# ─────────────────────────────────────────────
notify_discord() {
    local file="$1"
    local elapsed_seconds="$2"

    local file_name
    file_name=$(basename "$file")

    local path_on_server
    path_on_server=$(upload_file "$file" "$file_name")
    local out_file="$HOST$path_on_server"
    echo "URL file: $out_file"

    local qr_path
    echo "DEBUG: Truy cập QR từ: $FILE_SERVICE/qr?text=$out_file"
    qr_path=$(curl -sS "$FILE_SERVICE/qr?text=$out_file" 2>&1)
    echo "DEBUG: qr_path result: $qr_path"
    local qr="$HOST$qr_path"
    echo "QR: $qr"

    local size
    size=$(ls -lh "$file" | awk '{print $5}')

    local msg
    msg=$(printf "File: [%s](%s)\nTag: *%s*\nBranch: *%s*\nSize: *%s*\nQR: [Quét để tải](%s)\nBuild time: *%ss*" \
        "$file_name" "$out_file" \
        "$newTag" "$current_branch" \
        "$size" "$qr" \
        "$elapsed_seconds")

    local JSON_PAYLOAD
    JSON_PAYLOAD=$(jq -n \
        --arg username "${GIT_AUTHOR_NAME:-Jenkins}" \
        --arg avatar_url "https://mirrors.tuna.tsinghua.edu.cn/jenkins/art/jenkins-logo/256x256/headshot.png" \
        --arg title "✅ Build Success - $current_branch" \
        --arg url "$out_file" \
        --arg description "$msg" \
        --arg image_url "$qr" \
        '{username: $username, avatar_url: $avatar_url, embeds: [{title: $title, url: $url, description: $description, color: 3066993, image: {url: $image_url}}]}')

    if [ "$current_branch" == "main" ] || [ "$current_branch" == "release" ]; then
        curl -sS -H 'Content-Type: application/json' -X POST -d "$JSON_PAYLOAD" "$DISCORD_WEBHOOK_PRODUCTION"
    else
        curl -sS -H 'Content-Type: application/json' -X POST -d "$JSON_PAYLOAD" "$DISCORD_WEBHOOK_TESTING"
    fi
}

notify_discord_failure() {
    local commit="${GIT_COMMIT:-$(git log -1 --pretty=format:'%h - %s')}"
    local build_url="${BUILD_URL:-}"

    local JSON_PAYLOAD
    JSON_PAYLOAD=$(jq -n \
        --arg username "${GIT_AUTHOR_NAME:-Jenkins}" \
        --arg avatar_url "https://mirrors.tuna.tsinghua.edu.cn/jenkins/art/jenkins-logo/256x256/headshot.png" \
        --arg title "❌ Build THẤT BẠI - $current_branch" \
        --arg description "Branch: \`$current_branch\`\nCommit: \`$commit\`\nBuild: [#${BUILD_NUMBER:-?}]($build_url)" \
        '{username: $username, avatar_url: $avatar_url, embeds: [{title: $title, description: $description, color: 15158332}]}')

    if [ "$current_branch" == "main" ] || [ "$current_branch" == "release" ]; then
        curl -sS -H 'Content-Type: application/json' -X POST -d "$JSON_PAYLOAD" "$DISCORD_WEBHOOK_PRODUCTION"
    else
        curl -sS -H 'Content-Type: application/json' -X POST -d "$JSON_PAYLOAD" "$DISCORD_WEBHOOK_TESTING"
    fi
}

# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────

# Gửi Discord khi script thoát vì lỗi bất kỳ
trap 'notify_discord_failure; exit 1' ERR

# Tính version mới
newTag=$(increase_tag "$current_branch")
IFS='.' read -r vmain vrelease vtesting vdevelop <<< "$newTag"

# Chuẩn hóa các thành phần (mặc định là 0 nếu thiếu) và loại bỏ số 0 ở đầu để tránh lỗi octal
vmain=$((10#${vmain:-0}))
vrelease=$((10#${vrelease:-0}))
vtesting=$((10#${vtesting:-0}))
vdevelop=$((10#${vdevelop:-0}))

# Tính versionCode: (vmain * 1000000) + (vrelease * 10000) + (vtesting * 100) + vdevelop
# Luôn đảm bảo versionCode >= 1 vì Android yêu cầu giá trị dương
calculated_version_code=$(( vmain * 1000000 + vrelease * 10000 + vtesting * 100 + vdevelop ))
if [ "$calculated_version_code" -le 0 ]; then
    calculated_version_code=1
fi

echo "Version mới: $newTag (main=$vmain, release=$vrelease, testing=$vtesting, develop=$vdevelop) → versionCode=$calculated_version_code"

# Cập nhật gradle
update_version_in_gradle "$newTag" "$calculated_version_code"

# Build
run_builder

echo "--- Cây output ---"
find app/build -type f | sort

# Kiểm tra có APK/AAB không
apk_count=$(find app/build -type f \( -name "*.apk" -o -name "*.aab" \) | wc -l | tr -d ' ')
if [ "$apk_count" -eq 0 ]; then
    echo "Không tìm thấy APK/AAB sau khi build!"
    notify_discord_failure
    exit 1
fi

# Tag & push
auto_create_tag

# Tắt trap trước khi gửi success
trap - ERR

# Tính thời gian build
end_time=$(date +%s)
elapsed_seconds=$((end_time - start_time))

# Gửi từng file APK/AAB lên Discord
for file in $(find app/build -type f \( -name "*.apk" -o -name "*.aab" \)); do
    echo "Upload: $file"
    notify_discord "$file" "$elapsed_seconds"
done

echo "================================================"
echo " Build hoàn tất: v$newTag ($elapsed_seconds giây)"
echo "================================================"
