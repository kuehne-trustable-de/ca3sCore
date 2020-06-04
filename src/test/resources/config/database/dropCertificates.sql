SET FOREIGN_KEY_CHECKS = 0;


TRUNCATE TABLE ca3s_jh_dev.certificate;
TRUNCATE TABLE ca3s_jh_dev.certificate_attribute;
TRUNCATE TABLE ca3s_jh_dev.csr_attribute;
TRUNCATE TABLE ca3s_jh_dev.csr_attribute;
TRUNCATE TABLE ca3s_jh_dev.imported_url;

SET FOREIGN_KEY_CHECKS = 1;

commit;
