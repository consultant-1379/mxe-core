#!/bin/bash
set -x
mxe_version=$(echo "$1"|awk -F'-' '{print $1}')

init_path="$(dirname "$0")/../../"
reports_dir="$init_path/.bob/va-reports/"
va_config="$init_path/scan-config/va-report.config"
va_html_config="$init_path/scan-config/va-report-html.config"
output_report="$reports_dir/Machine_Learning_Execution_Environment-$mxe_version-VAreport.md"
anchore_path="$reports_dir/anchore"
trivy_path="$reports_dir/trivy"
nmap_path="$reports_dir/nmap/nmap_report"
kubeaudit_report_path="$reports_dir/kube-audit-report/arm_charts/"
xray_report="$reports_dir/xray-reports/xray_report.json"
raw_xray_report="$reports_dir/xray-reports/raw_xray_report.json"
va_html_report_path="$init_path/html_report/"

echo "Updating version in config" 
sed -i "/\<version\>:.*/ s/:.*/: $mxe_version/g" "$va_config"
options=""

if [ -d "$reports_dir"/anchore ]
then
    options="$options --anchore $anchore_path"
fi

if [ -d "$reports_dir"/trivy ]
then
    options="$options --trivy $trivy_path"
fi

if [ -f "$xray_report" ]
then
    options="$options --xray-report $xray_report"
fi

if [ -f "$raw_xray_report" ]
then
    options="$options --raw-xray-report $raw_xray_report"
fi

if [ -d "$nmap_path" ]
then
    options="$options --nmap $nmap_path"
fi

kubeaudit_files=$(find "$kubeaudit_report_path" -type f -size +0)

for f in $kubeaudit_files
do
    options="$options --kubeaudit-reports $f"
done

echo "Executing va-report automation tool"

bash -c "va-report --config $va_config  --output $output_report --md --debug $options"

echo "Generating HTML report"
sed -i "s|Report_Name|$output_report|g" "$va_html_config"
bash -c "doc-handler generate --config $va_html_config  --output $va_html_report_path  --format html"