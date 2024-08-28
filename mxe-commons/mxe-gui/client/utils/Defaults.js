import {
  ERROR,
  JOB_STATUS_COMPLETED,
  JOB_STATUS_FAILED,
  JOB_STATUS_RUNNING,
  STATUS_AVAILABLE,
  STATUS_CREATING,
  STATUS_DEPLOYMENT_ERROR,
  STATUS_ERROR,
  STATUS_PACKAGING,
  STATUS_RUNNING,
  SUCCESS,
} from 'utils/Enums';
import { loc } from 'utils/Utils';
import { IS_STANDALONE_STR } from './Config';

export const MAX_NUMBER_OF_INSTANCES = 99;
export const DASHBOARD_MODELS_LENGTH = 5;
export const DEFAULT_INTERVAL_MS = 60000;
export const DEFAULT_CHART_UPDATE_INTERVAL_MS = 15000;
export const NOTIFICATION_TIMEOUT = 10000;
export const MAX_TEXT_LENGTH = 100;
export const MODEL_DEPLOYMENT_NAME_REGEXP = /^[a-z0-9]+(-[a-z0-9]+)*$/;
export const MODEL_DEPLOYMENT_NAME_REGEXP_STRING = '^[a-z0-9]+(-[a-z0-9]+)*$';
export const PLACEHOLDER_IMAGE =
  'data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==';

export const SORTING_OPTIONS = [
  { name: loc('NEWEST_TO_OLDEST'), by: 'date', order: 'desc' },
  { name: loc('OLDEST_TO_NEWEST'), by: 'date', order: 'asc' },
  { name: loc('ALPHABETICAL_A_Z'), by: 'name', order: 'desc' },
  { name: loc('ALPHABETICAL_Z_A'), by: 'name', order: 'asc' },
  { name: loc('STATUS_A_Z'), by: 'status', order: 'desc' },
  { name: loc('STATUS_Z_A'), by: 'status', order: 'asc' },
];
export const SORTING_VERSIONS = [
  { name: loc('NEWEST_TO_OLDEST'), by: 'date', order: 'desc' },
  { name: loc('OLDEST_TO_NEWEST'), by: 'date', order: 'asc' },
  { name: loc('STATUS_A_Z'), by: 'status', order: 'desc' },
  { name: loc('STATUS_Z_A'), by: 'status', order: 'asc' },
];
export const SORTING_OPTIONS_INFO_PAGE = [
  { name: loc('ALPHABETICAL_A_Z'), by: 'name', order: 'desc' },
  { name: loc('ALPHABETICAL_Z_A'), by: 'name', order: 'asc' },
];

export const DEFAULT_FILTERS = [
  { name: STATUS_PACKAGING, label: loc('PACKAGING'), checked: false },
  { name: STATUS_AVAILABLE, label: loc('AVAILABLE'), checked: false },
  { name: STATUS_ERROR, label: loc('PACKAGING_FAILED'), checked: false },
];

export const MODEL_SERVICES_FILTERS = [
  { name: STATUS_RUNNING, label: loc('RUNNING'), checked: false },
  { name: STATUS_CREATING, label: loc('CREATING'), checked: false },
  { name: STATUS_DEPLOYMENT_ERROR, label: loc('ERROR'), checked: false },
];

export const TRAINING_PACKAGES_FILTERS = [
  { name: STATUS_AVAILABLE, label: loc('AVAILABLE'), checked: false },
  { name: STATUS_PACKAGING, label: loc('PACKAGING'), checked: false },
  { name: STATUS_ERROR, label: loc('ERROR'), checked: false },
];

export const TRAINING_JOBS_FILTERS = [
  { name: JOB_STATUS_FAILED, label: loc('FAILED'), checked: false },
  { name: JOB_STATUS_RUNNING, label: loc('RUNNING'), checked: false },
  { name: JOB_STATUS_COMPLETED, label: loc('COMPLETED'), checked: false },
];

export const NOTEBOOK_FILTERS = [];

export const DRAG_DROP_EVENTS = [
  'drag',
  'dragstart',
  'dragend',
  'dragover',
  'dragenter',
  'dragleave',
  'drop',
];

export const MODEL_DETAIL_MENUS = [
  {
    name: 'MODEL_INFORMATION',
    title: loc('MODEL_INFORMATION'),
    isOpened: true,
  },
  // {
  //   name: 'MONITORING_INFORMATION',
  //   title: loc('MONITORING_INFORMATION'),
  //   isOpened: true,
  // },
  // {
  //   name: 'CUSTOM_METRICS',
  //   title: loc('CUSTOM_METRICS'),
  //   isOpened: true,
  // },
  // {
  //   name: 'INVOKE_MODEL',
  //   title: loc('INVOKE_MODEL'),
  //   isOpened: false,
  // },
  // {
  //   name: 'REWARD',
  //   title: loc('REWARD'),
  //   isOpened: true,
  // },
];

export const MONITORING_CHART_LEGEND = ['min', 'max', 'avg', 'current'];

export const MODEL_INFO_TABLE = [
  { name: loc('MODEL_ID'), prop: 'id' },
  { name: loc('MODEL_TITLE'), prop: 'title' },
  { name: loc('MODEL_VERSION'), prop: 'version' },
  { name: loc('MODEL_DESCRIPTION'), prop: 'description' },
];

export const DEFAULT_ERROR_NOTIFICATION = {
  title: loc('ERROR_OCCURRED'),
  description: loc('CHECK_ERROR'),
  status: ERROR,
  timeout: 3000,
};

export const DEFAULT_SUCCESSFUL_DELETE_NOTIFICATION = {
  title: loc('SUCCESSFULLY_DELETED'),
  status: SUCCESS,
  description: '',
  timeout: 3000,
};

// Interval in minutes
export const MONITORING_INTERVALS = [
  { name: loc('LAST_5_MINUTES'), value: 5 },
  { name: loc('LAST_HOUR'), value: 60 },
  { name: loc('LAST_SIX_HOURS'), value: 360 },
  { name: loc('LAST_TWELVE_HOURS'), value: 720 },
  { name: loc('LAST_DAY'), value: 1440 },
  { name: loc('LAST_WEEK'), value: 10080 },
];

export const DEFAULT_CHART_TITLES = [
  { name: loc('REQUEST_RATE'), unit: 'req/s' },
  { name: loc('LATENCY'), unit: 'ms' },
];

export const DEFAULT_MODEL_INVOKE = { data: { ndarray: [] } };

export const DEFAULT_JOB_TABLE_COLUMNS = [
  { title: loc('JOB_ID'), attribute: 'col1', sortable: true },
  { title: loc('JOB_CREATED'), attribute: 'col2', sortable: true },
  { title: loc('JOB_STATUS'), attribute: 'col3', sortable: true },
  { title: loc('JOB_COMPLETED'), attribute: 'col4', sortable: true },
  { title: '', attribute: 'col5', sortable: false, width: '150px' },
  { title: '', attribute: 'col6', sortable: false, width: '50px' },
];

export const SERVICES_TABLE_COLUMNS = [
  { title: loc('SERVICE_NAME'), attribute: 'col1', sortable: true },
  { title: loc('STATUS'), attribute: 'col3', sortable: true },
  { title: loc('INSTANCES'), attribute: 'col4', sortable: true },
  { title: loc('MODELS'), attribute: 'col5', sortable: true },
  { title: loc('MODEL_TYPE'), attribute: 'col6', sortable: true },
  { title: loc('CREATED_AT'), attribute: 'col7', sortable: true, width: '200px' },
  { title: loc('CREATED_BY'), attribute: 'col8', sortable: true },
];

export const PACKAGES_TABLE_COLUMNS = [
  { title: loc('PACKAGE_TITLE'), attribute: 'col1', sortable: true },
  { title: loc('LATEST_VERSION_ONBOARDDED_AT'), attribute: 'col2', sortable: true },
  { title: loc('ONBOARDED_BY'), attribute: 'col3', sortable: true },
  { title: loc('ONBOARDING_STATE'), attribute: 'col4', sortable: true },
  { title: loc('PACKAGE_ID'), attribute: 'col5', sortable: true, width: '200px' },
  { title: loc('VERSION'), attribute: 'col6', sortable: true },
];

export const VERSIONS_TABLE_COLUMNS = [
  { title: loc('STATUS'), attribute: 'col1', sortable: true },
  { title: loc('VERSION'), attribute: 'col2', sortable: true },
  { title: loc('DATE_OF_ONBOARDING'), attribute: 'col3', sortable: true },
  { title: '', attribute: 'col4', sortable: false, width: '50px' },
];

export const MODELS_TABLE_COLUMNS = [
  { title: loc('MODEL_TITLE'), attribute: 'col1', sortable: true },
  { title: loc('MODEL_ID'), attribute: 'col2', sortable: true },
  { title: loc('MODEL_VERSIONS'), attribute: 'col3', sortable: false },
  { title: loc('ONBOARDING_STATE'), attribute: 'col4', sortable: true },
  { title: loc('LATEST_VERSION_ONBOARDDED_AT'), attribute: 'col5', sortable: true },
  { title: loc('ONBOARDED_BY'), attribute: 'col6', sortable: true },
];

export const BOOTSTRAP_CONF = [
  { title: loc('PROPERTY_NAME'), attribute: 'col1', sortable: true },
  { title: loc('PROPERTY_VALUE'), attribute: 'col2', sortable: true },
  { title: loc('ACTION'), attribute: 'col3', sortable: false },
];

export const BOOTSTRAP_CONF_DISPLAY = [
  { title: loc('PROPERTY_NAME'), attribute: 'col1', sortable: true },
  { title: loc('PROPERTY_VALUE'), attribute: 'col2', sortable: true },
];

export const AUTHOR_TABLE_COLUMNS = [
  { title: loc('AUTHOR_NAME'), attribute: 'col1', sortable: true },
  { title: loc('PUBLIC_KEY'), attribute: 'col2', sortable: true },
  { title: '', attribute: 'col3', sortable: false },
];

export const NOTEBOOK_TABLE_COLUMNS = [{ title: loc('NAME'), attribute: 'col1', sortable: true }];
export const TARGET_METRIC_PLACEHOLDER = loc('SELECT_TARGET_METRIC');

export const SERVICE_LOG_QUERY_UNITS = [
  { title: loc('MINUTES'), unit: 'm' },
  { title: loc('HOURS'), unit: 'h' },
  { title: loc('LINES'), unit: 'l' },
];

export const SERVICE_LOG_QUICK_SEARCH_ITEMS = [
  { title: `10 ${loc('MINUTES')}`, value: '10', unit: SERVICE_LOG_QUERY_UNITS[0] },
  { title: `30 ${loc('MINUTES')}`, value: '30', unit: SERVICE_LOG_QUERY_UNITS[0] },
  { title: `1 ${loc('HOURS')}`, value: '1', unit: SERVICE_LOG_QUERY_UNITS[1] },
  { title: `3 ${loc('HOURS')}`, value: '3', unit: SERVICE_LOG_QUERY_UNITS[1] },
  { title: `8 ${loc('HOURS')}`, value: '8', unit: SERVICE_LOG_QUERY_UNITS[1] },
  { title: `12 ${loc('HOURS')}`, value: '12', unit: SERVICE_LOG_QUERY_UNITS[1] },
  { title: `24 ${loc('HOURS')}`, value: '24', unit: SERVICE_LOG_QUERY_UNITS[1] },
  { title: `Maximum ${loc('LINES')}`, value: '50000', unit: SERVICE_LOG_QUERY_UNITS[2] },
];
