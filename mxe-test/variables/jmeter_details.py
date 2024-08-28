import os

# JMETER WORKING DIRECTORY
jmeter_working_dir = os.getenv("JMETER_WORKING_DIR", "/Users/eilmnqq")

# Jmeter Install Path
jmeter_install_path = f'{jmeter_working_dir}/apache-jmeter-5.4.1/bin/jmeter'

# TEST DATA DIR
test_data_dir = os.getenv("TEST_DATA_DIR", "/Users/eilmnqq/apache-jmeter-5.4.1")

# Jmeter JMX File Path
jmeter_jmx_path = f'{test_data_dir}/sample_test.jmx'

# JMETER REPORT DIRECTORY
jmeter_report_dir = os.getenv("ROBOT_REPORTS_DIR", "/Users/eilmnqq/apache-jmeter-5.4.1")

# Jmeter Log Path
jmeter_log_path = f'{jmeter_report_dir}/test_log.jtl'

# Jmeter Install Path
# jmeter_install_path = '/Users/eilmnqq/apache-jmeter-5.4.1/bin/jmeter'

# Jmeter JMX File Path
# jmeter_jmx_path = '/Users/eilmnqq/apache-jmeter-5.4.1/sample_test.jmx'

# Jmeter Log Path
# jmeter_log_path = '/Users/eilmnqq/apache-jmeter-5.4.1/test_log.jtl'
