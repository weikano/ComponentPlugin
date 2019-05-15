package com.wkswind.plugin.component.api.utils;

import java.io.File;

public class Consts {
  public static final String COMPONENT_GEN_PACKAGENAME = "com.wkswind.plugin.component.gen";

  static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
  static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;

  static final String PREFS_FILE = ".component.multidex.version";
  static final String KEY_DEX_NUMBER = "dex.number";

  static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";
  static final String EXTRACTED_SUFFIX = ".zip";

  static final String COMPONENT_SP = ".sp_component_cache";
  static final String GENS_CACHE = ".sp_component_gens";
  static final String LAST_VERSION_NAME = "sp_component_vname";
  static final String LAST_VERSION_CODE = "sp_component_vcode";
}
