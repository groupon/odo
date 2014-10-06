/*
 Copyright 2014 Groupon, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
package com.groupon.odo.proxylib;

public class Constants {
    // version information
    public static String VERSION = "";

    public static final String PATH_PROFILE_DEFAULT_CONTENT_TYPE = "application/json;charset=utf-8";

    // System config
    public static final String SYS_API_PORT = "ODO_API_PORT";
    public static final String SYS_DB_PORT = "ODO_DB_PORT";
    public static final String SYS_FWD_PORT = "ODO_FWD_PORT";
    public static final String SYS_HTTP_PORT = "ODO_HTTP_PORT";
    public static final String SYS_HTTPS_PORT = "ODO_HTTPS_PORT";
    public static final int DEFAULT_API_PORT = 8090;
    public static final int DEFAULT_DB_PORT = 9092;
    public static final int DEFAULT_FWD_PORT = 9090;
    public static final int DEFAULT_HTTP_PORT = 8082;
    public static final int DEFAULT_HTTPS_PORT = 8012;
    public static final String ODO_PROXY_HEADER = "X-ODO-REQUEST";
    public static final String ODO_INTERNAL_WEBAPP_URL = "http://odo";

    // DB constants
    public static final int DB_CURRENT_SCHEMA_VERSION = 4;
    public static final String DB_TABLE_CONFIGURATION = "configuration";
    public static final String DB_TABLE_HISTORY = "history";
    public static final String DB_TABLE_OVERRIDE = "override_db";
    public static final String DB_TABLE_SERVERS = "server_redirect_db";
    public static final String DB_TABLE_PATH = "path_profile_db";
    public static final String DB_TABLE_ENABLED_OVERRIDE = "enabled_overrides_db";
    public static final String DB_TABLE_REQUEST_RESPONSE = "request_response_db";
    public static final String DB_TABLE_PROFILE = "profile_db";
    public static final String DB_TABLE_CLIENT = "client_db";
    public static final String DB_TABLE_GROUPS = "groups_db";
    public static final String DB_TABLE_SCRIPT = "script_db";
    public static final String DB_TABLE_SERVER_GROUPS = "server_group_db";

    // App configuration constants
    public static final String DB_TABLE_CONFIGURATION_DATABASE_VERSION = "database_version";
    public static final String DB_TABLE_CONFIGURATION_PLUGIN_PATH = "plugin_path";
    public static final String DB_TABLE_CONFIGURATION_NAME = "name";
    public static final String DB_TABLE_CONFIGURATION_VALUE = "value";

    // request type constants
    public static final int REQUEST_TYPE_ALL = 0;
    public static final int REQUEST_TYPE_GET = 1;
    public static final int REQUEST_TYPE_PUT = 2;
    public static final int REQUEST_TYPE_POST = 3;
    public static final int REQUEST_TYPE_DELETE = 4;

    // script types
    public static final int SCRIPT_TYPE_HISTORY = 0;

    // JSONP constants
    public static final String[] JSONP_CALLBACK_NAMES = {"callback", "jsonp"};

    // Plugin information
    public static final int PLUGIN_STATUS_VALID = 0;
    public static final int PLUGIN_STATUS_NOT_DIRECTORY = 1;
    public static final int PLUGIN_RESPONSE_OVERRIDE_CUSTOM = -1;
    public static final int PLUGIN_REQUEST_OVERRIDE_CUSTOM = -2;
    public static final int PLUGIN_RESPONSE_HEADER_OVERRIDE_ADD = -3;
    public static final int PLUGIN_RESPONSE_HEADER_OVERRIDE_REMOVE = -4;
    public static final int PLUGIN_REQUEST_HEADER_OVERRIDE_ADD = -5;
    public static final int PLUGIN_REQUEST_HEADER_OVERRIDE_REMOVE = -6;
    public static final String PLUGIN_TYPE_RESPONSE_OVERRIDE = "interface com.groupon.odo.plugin.ResponseOverride";
    public static final String PLUGIN_TYPE_RESPONSE_OVERRIDE_V2 = "interface com.groupon.odo.plugin.v2.ResponseOverride";
    public static final String PLUGIN_TYPE_REQUEST_OVERRIDE = "interface com.groupon.odo.plugin.RequestOverride";

    // Override information
    public static final String DB_TABLE_OVERRIDE_METHOD_NAME = "method_name";
    public static final String DB_TABLE_OVERRIDE_CLASS_NAME = "class_name";

    // Path information
    public static final String DB_TABLE_PATH_RESPONSE_ENABLED = "response_enabled";
    public static final String DB_TABLE_PATH_REQUEST_ENABLED = "request_enabled";

    // Plugin Annotation information
    public static final String PLUGIN_RESPONSE_OVERRIDE_CLASS = "com.groupon.odo.plugin.ResponseOverride";
    public static final String PLUGIN_RESPONSE_OVERRIDE_V2_CLASS = "com.groupon.odo.plugin.v2.ResponseOverride";
    public static final String PLUGIN_REQUEST_OVERRIDE_CLASS = "com.groupon.odo.plugin.RequestOverride";
    public static final String PLUGIN_RESPONSE_OVERRIDE_HTTP_CODE = "httpCode";
    public static final String PLUGIN_RESPONSE_OVERRIDE_DESCRIPTION = "description";

    // Header constants
    public static final String HEADER_STATUS = "status";
    public static final String HEADER_ACCEPT = "accept";
    public static final String HEADER_ACCEPT_ENCODING = "accept-encoding";

    // History constants
    public static final String HISTORY_FILTER_SOURCE_URI = "source_uri";
    public static final String HISTORY_CREATED_AT = "created_at";
    public static final String HISTORY_REQUEST_URL = "request_url";
    public static final String HISTORY_REQUEST_PARAMS = "request_params";
    public static final String HISTORY_REQUEST_POST_DATA = "request_post_data";
    public static final String HISTORY_REQUEST_HEADERS = "request_headers";
    public static final String HISTORY_RESPONSE_CODE = "response_code";
    public static final String HISTORY_RESPONSE_HEADERS = "response_headers";
    public static final String HISTORY_RESPONSE_CONTENT_TYPE = "response_content_type";
    public static final String HISTORY_RESPONSE_DATA = "response_data";
    public static final String HISTORY_ORIGINAL_REQUEST_URL = "original_request_url";
    public static final String HISTORY_ORIGINAL_REQUEST_PARAMS = "original_request_params";
    public static final String HISTORY_ORIGINAL_REQUEST_POST_DATA = "original_request_post_data";
    public static final String HISTORY_ORIGINAL_REQUEST_HEADERS = "original_request_headers";
    public static final String HISTORY_ORIGINAL_RESPONSE_CODE = "original_response_code";
    public static final String HISTORY_ORIGINAL_RESPONSE_HEADERS = "original_response_headers";
    public static final String HISTORY_ORIGINAL_RESPONSE_CONTENT_TYPE = "original_response_content_type";
    public static final String HISTORY_ORIGINAL_RESPONSE_DATA = "original_response_data";
    public static final String HISTORY_MODIFIED = "modified";
    public static final String HISTORY_REQUEST_SENT = "requestSent";


    // Client profile constants
    public static final String PROFILE_CLIENT_DEFAULT_ID = "-1";
    public static final String PROFILE_CLIENT_HEADER_NAME = "Odo-Client-UUID";

    //Table column names
    //profile_db
    public static final String PROFILE_PROFILE_NAME = "profile_name";

    // generic
    public static final String GENERIC_CLIENT_UUID = "client_uuid";
    public static final String GENERIC_PROFILE_ID = "profile_id";
    public static final String GENERIC_ID = "id";
    public static final String GENERIC_REQUEST_TYPE = "request_type";
    public static final String GENERIC_NAME = "name";

    //client_db
    public static final String CLIENT_CLIENT_UUID = "client_uuid";
    public static final String CLIENT_IS_ACTIVE = "is_active";
    public static final String CLIENT_PROFILE_ID = "profile_id";
    public static final String CLIENT_FRIENDLY_NAME = "friendly_name";
    public static final String CLIENT_ACTIVESERVERGROUP = "active_server_group";
    public static final int CLIENT_CLIENTS_PER_PROFILE_LIMIT = 300;

    //server_redirect_db
    public static final String SERVER_REDIRECT_REGION = "region";
    public static final String SERVER_REDIRECT_SRC_URL = "src_url";
    public static final String SERVER_REDIRECT_DEST_URL = "dest_url";
    public static final String SERVER_REDIRECT_HOST_HEADER = "host_header";
    public static final String SERVER_REDIRECT_PROFILE_ID = "profile_id";
    public static final String SERVER_REDIRECT_GROUP_ID = "group_id";

    //path_profile_db
    public static final String PATH_PROFILE_PATHNAME = "pathname";
    public static final String PATH_PROFILE_ACTUAL_PATH = "actual_path";
    public static final String PATH_PROFILE_BODY_FILTER = "body_filter";
    public static final String PATH_PROFILE_GROUP_IDS = "group_ids";
    public static final String PATH_PROFILE_PROFILE_ID = "profile_id";
    public static final String PATH_PROFILE_PATH_ORDER = "path_order";
    public static final String PATH_PROFILE_CONTENT_TYPE = "content_type";
    public static final String PATH_PROFILE_REQUEST_TYPE = "request_type";
    public static final String PATH_PROFILE_GLOBAL = "global";

    // request_response_db
    public static final String REQUEST_RESPONSE_PATH_ID = "path_id";
    public static final String REQUEST_RESPONSE_REPEAT_NUMBER = "repeat_number";
    public static final String REQUEST_RESPONSE_REQUEST_ENABLED = "request_enabled";
    public static final String REQUEST_RESPONSE_RESPONSE_ENABLED = "response_enabled";
    public static final String REQUEST_RESPONSE_CUSTOM_RESPONSE = "custom_response";
    public static final String REQUEST_RESPONSE_CUSTOM_REQUEST = "custom_request";

    // groups_db
    public static final String GROUPS_GROUP_NAME = "group_name";

    // override_db
    public static final String OVERRIDE_METHOD_NAME = "method_name";
    public static final String OVERRIDE_CLASS_NAME = "class_name";
    public static final String OVERRIDE_GROUP_ID = "group_id";

    public static final int OVERRIDE_TYPE_RESPONSE = -1;
    public static final int OVERRIDE_TYPE_REQUEST = -2;

    // enabaled_overrides
    public static final String ENABLED_OVERRIDES_PATH_ID = "path_id";
    public static final String ENABLED_OVERRIDES_OVERRIDE_ID = "override_id";
    public static final String ENABLED_OVERRIDES_PRIORITY = "priority";
    public static final String ENABLED_OVERRIDES_ARGUMENTS = "arguments";
    public static final String ENABLED_OVERRIDES_REPEAT_NUMBER = "repeat_number";

    // script_db
    public static final String SCRIPT_NAME = "name";
    public static final String SCRIPT_SCRIPT = "script";
    public static final String SCRIPT_TYPE = "type";
}
