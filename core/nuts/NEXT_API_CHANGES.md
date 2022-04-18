TODO in API (planned for 0.8.4):
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