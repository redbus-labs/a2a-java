import json
import sys
from datetime import datetime

def generate_markdown(json_file, output_file):
    with open(json_file, 'r') as f:
        data = json.load(f)

    meta = data.get('meta', {})
    features = data.get('features', [])

    with open(output_file, 'w') as f:
        f.write("# A2A Java Parity Status\n\n")
        f.write(f"**Last Updated:** {meta.get('last_updated', datetime.now().isoformat())}\n")
        f.write(f"**Python Reference:** [{meta.get('python_reference_commit')[:7]}](https://github.com/a2aproject/{meta.get('python_reference_repo')}/commit/{meta.get('python_reference_commit')})\n")
        f.write(f"**Java Version:** {meta.get('java_version')}\n\n")

        f.write("## Feature Implementation Matrix\n\n")
        f.write("| Feature | Status | Python Reference | Java Implementation | Notes |\n")
        f.write("| :--- | :---: | :--- | :--- | :--- |\n")

        for feature in features:
            status_symbol = {
                'DONE': '✅',
                'PARTIAL': '⚠️',
                'MISSING': '❌',
                'N/A': '➖'
            }.get(feature['status'], feature['status'])

            f.write(f"| **{feature['name']}** | {status_symbol} {feature['status']} | `{feature['python_ref']}` | `{feature['java_impl']}` | {feature.get('notes', '')} |\n")

        f.write("\n\n---\n*Generated automatically by `generate_parity_report.py`*")

if __name__ == "__main__":
    generate_markdown('parity.json', 'PARITY_STATUS.md')
    print("Generated PARITY_STATUS.md from parity.json")
