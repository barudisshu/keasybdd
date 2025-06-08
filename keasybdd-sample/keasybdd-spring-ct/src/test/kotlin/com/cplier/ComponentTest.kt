package com.cplier

import io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME
import io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("feature")
@ConfigurationParameter(
  key = GLUE_PROPERTY_NAME,
  value = "com.cplier",
)
@ConfigurationParameter(
  key = PLUGIN_PROPERTY_NAME,
  value = "pretty,html:build/cucumber-reports/report.html,json:build/cucumber-reports/report.json",
)
public class ComponentTest
