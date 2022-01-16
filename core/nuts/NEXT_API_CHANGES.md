TODO in API (planned for 0.8.4):
+ Add NutsEnvCondition/NutsEnvConditionBuilder::setProperties/getProperties
+ Add NutsDescriptor/NutsDescriptorBuilder::setLicenses
+ Add NutsDescriptor/NutsDescriptorBuilder::setDevelopers
+ Rename NutsConstants.IdProperties.DESKTOP_ENVIRONMENT -> NutsConstants.IdProperties.DESKTOP
+ Add NutsConstants.IdProperties.DESKTOP_ENVIRONMENT -> NutsConstants.IdProperties.PROPERTIES
+ Rename NutsRepositoryDB::getRepositoryNameByURL -> NutsRepositoryDB::getRepositoryNameByLocation
+ Rename NutsRepositoryDB::getRepositoryURLByName -> NutsRepositoryDB::getRepositoryLocationByName
+ change type to long in NutsExecCommand::getSleepMillis()/setSleepMillis(int sleepMillis);
+ remove PrivateNutsUtilBootId::parseBootIdList;