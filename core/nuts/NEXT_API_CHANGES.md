TODO in API (planned for 0.8.4):
+ Refactor NutsTextParser to support NPath and remove NutsTextFormatLoader as well as the followings...
   + NutsText parseResource(String resourceName, NutsTextFormatLoader loader);
   + NutsText parseResource(String resourceName, Reader reader, NutsTextFormatLoader loader);
   + NutsTextFormatLoader createLoader(ClassLoader loader);
   + NutsTextFormatLoader createLoader(File root);
   + NutsStream::sorted must take comparator and descriptor!!
   + Add NutsElement::asBigInteger 
   + Add NutsElement::asBigDecimal 
