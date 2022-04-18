TODO in API (planned for 0.8.4):
+ Remove session dependency from NutsVersion,NutsId and NutsDescriptor
+ Enum.parseLenient replaced by NutsOptional
+ Add NutsDescriptor/NutsDescriptorBuilder::setLicenses
+ Add NutsDescriptor/NutsDescriptorBuilder::setDevelopers
+ Rename NutsConstants.IdProperties.DESKTOP_ENVIRONMENT -> NutsConstants.IdProperties.DESKTOP
+ Add NutsConstants.IdProperties.DESKTOP_ENVIRONMENT -> NutsConstants.IdProperties.PROPERTIES
+ Rename NutsRepositoryDB::getRepositoryNameByURL -> NutsRepositoryDB::getRepositoryNameByLocation
+ Rename NutsRepositoryDB::getRepositoryURLByName -> NutsRepositoryDB::getRepositoryLocationByName
+ change type to long in NutsExecCommand::getSleepMillis()/setSleepMillis(int sleepMillis);
+ remove PrivateNutsUtilBootId::parseBootIdList;
+ Refactor NutsTextParser to support NutsPath and remove NutsTextFormatLoader as well as the followings...
   + NutsText parseResource(String resourceName, NutsTextFormatLoader loader);
   + NutsText parseResource(String resourceName, Reader reader, NutsTextFormatLoader loader);
   + NutsTextFormatLoader createLoader(ClassLoader loader);
   + NutsTextFormatLoader createLoader(File root);
   + NutsStream::sorted must take comparator and descriptor!!
   + Add NutsElement::asBigInteger 
   + Add NutsElement::asBigDecimal 
+ Add NutsPath::getLongBaseName // longest file name before last '.'