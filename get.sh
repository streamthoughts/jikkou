#!/bin/bash

set -e

temp=/tmp/jikkou-download-$(date +"%s")
# Get latest version
url="https://api.github.com/repos/streamthoughts/jikkou/releases/latest"
latest=$(curl -s $url | grep "browser_download_url.*deb" | cut -d : -f 2,3 | cut -d / -f 8 | uniq)

echo -e "Downloading: Jikkou $latest\n"

echo -e "In progress...\n"
# Download latest release
curl -s $url \
| grep "browser_download_url.*deb" \
| cut -d : -f 2,3 \
| tr -d \" \
| wget -qP "$temp" --show-progress -i -

# Verify signature
echo -e "\nVerifying signature: ($temp/jikkou.deb.asc)\n"
gpg --quiet --keyserver keyserver.ubuntu.com --recv-keys D12696955654914F 2>/dev/null
gpg --quiet --verify "$temp/jikkou.deb.asc" "$temp/jikkou.deb" 2>/dev/null

# Installing debian package
echo -e "\nInstalling: Jikkou $latest\n"
sudo dpkg -i "$temp/jikkou.deb"

echo -e "\nDone installing!\n"

source <(jikkou generate-completion)
jikkou --version

exit 0;