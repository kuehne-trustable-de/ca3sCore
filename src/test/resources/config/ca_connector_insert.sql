insert into ca3s_jh_dev.ca_connector_config
(id, name, ca_connector_type, ca_url, secret, polling_offset, default_ca, active) values
(1, 'InternalTestCA', 'Internal', 'http://localhost:${server.port}/cmpTest', 's3cr3t', 0, false, false);

insert into ca3s_jh_dev.ca_connector_config
(id, name, ca_connector_type, ca_url, secret, polling_offset, default_ca, active) values
(2, 'testCmpAlias', 'Cmp', 'http://10.1.0.10:8080/ejbca/publicweb/cmp', 's3cr3t', 0, false, true);

insert into ca3s_jh_dev.ca_connector_config
(id, name, ca_connector_type, ca_url, secret, polling_offset, default_ca, active) values
(3, 'adcsTest', 'Adcs', 'https://CA3S-MS-SUB-CA1:8088/ADCSConnector', 's3cr3t', 0, false, false);

insert into ca3s_jh_dev.ca_connector_config
(id, name, ca_connector_type, ca_url, secret, polling_offset, default_ca, active) values
(4, 'localAdcs', 'Adcs', 'https://localhost:23443/ADCSConnector', 's3cr3t', 0, true, true);