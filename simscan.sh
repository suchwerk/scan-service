#/bin/bash
# Simulated NAPS2-like script: simulate-scan.sh
INPUT_PDF="/home/jk/projects/zeno/zeno-multi/testdata/documents/HUI/HUI/1468198_DELIVERYNOTE.pdf"  # change this if your sample PDF is in another location
OUTPUT_FILE=""
COMMAND_LINE=""
FORMAT=""
PROFILE=""

# Parse command-line options using getopts
while getopts "o:f:p:" opt; do
  case ${opt} in
    o)
      OUTPUT_FILE="$OPTARG"
      ;;
    f)
      FORMAT="$OPTARG"
      ;;
    p)
      PROFILE="$OPTARG"
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

# Log the arguments
echo "OUTPUT_FILE = $OUTPUT_FILE"
echo "FORMAT = $FORMAT"
echo "PROFILE = $PROFILE"
if [[ -z "$OUTPUT_FILE" ]]; then
    echo "No output file specified with -o"
    exit 1
fi

# Simulate scanning by copying a file
if [[ ! -f "$INPUT_PDF" ]]; then
    echo "Sample PDF not found: $INPUT_PDF"
    exit 2
fi

# mkdir -p "$(dirname "$OUTPUT_FILE")"
echo "Copy from $INPUT_PDF to $OUTPUT_FILE"
cp "$INPUT_PDF" "$OUTPUT_FILE"

echo "Scan simulation complete. Output written to $OUTPUT_FILE"
