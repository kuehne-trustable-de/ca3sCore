INSERT INTO ca3s_jh_dev.ca_connector_config (name,ca_connector_type,ca_url,secret,polling_offset,default_ca,active,selector) VALUES 
('localAdcs','ADCS','https://localhost:23443/ADCSConnector','s3cr3t',3310,1,1,NULL)
,('localDirectoryScanner','DIRECTORY','file://e:\\tmp\\certs',NULL,NULL,0,1,NULL)
;