BEGIN TRANSACTION;
ALTER TABLE MerchantRolePermissionElectronicFundsTransfer add  [AllowCardholderPresent] BOOLEAN default 'false';
ALTER TABLE MerchantRolePermissionElectronicFundsTransfer add  [AllowMailOrder] BOOLEAN default 'false';
ALTER TABLE MerchantRolePermissionElectronicFundsTransfer add  [AllowTelephone] BOOLEAN default 'false';
ALTER TABLE MerchantRolePermissionElectronicFundsTransfer add  [AllowCardholderPresentUnattended] BOOLEAN default 'false';
ALTER TABLE MerchantDepartment add [MerchantReferenceSetting] INTEGER default '1';
COMMIT;
