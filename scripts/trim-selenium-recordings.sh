#!/bin/bash

# Define the target directory
RECORDINGS_DIR="./target/selenium-recordings"

# Check if the directory exists
if [ ! -d "$RECORDINGS_DIR" ]; then
  echo "Error: Directory '$RECORDINGS_DIR' not found."
  exit 1
fi

# Change to the recordings directory
cd "$RECORDINGS_DIR"

# Loop through all files with the .mp4 extension
for f in *.mp4; do
  # Check if a video file was found
  if [ -e "$f" ]; then
    echo "Processing and replacing file: $f"
    
    # Create a temporary file with the trimmed video
    # -ss 00:00:05 skips the first 5 seconds
    # -c:v copy and -c:a copy ensure no re-encoding, preserving quality and speed
    # -y automatically overwrites the output file if it already exists
    ffmpeg -y -i "$f" -ss 00:00:05 -c:v copy -c:a copy "temp_$f"
    
    # If the temporary file was created successfully, replace the original
    if [ -e "temp_$f" ]; then
      rm "$f" && mv "temp_$f" "$f"
      echo "Finished processing $f"
    else
      echo "Error: Failed to create temporary file for $f"
    fi
  else
    echo "No .mp4 files found in '$RECORDINGS_DIR'."
    break
  fi
done