package vn.chitieu.domain

import org.junit.Assert.*
import org.junit.Test

class AndroidSetupTest {
 @Test fun xiaomiGuidanceIsRestrictedToItsOwnBrands(){
  for(brand in listOf("Xiaomi"," POCO ","Redmi"))assertTrue(AndroidSetup.guides(brand,33).any {it.id=="autostart"})
  for(brand in listOf("Google","Samsung","OPPO","vivo","motorola","HONOR","unknown","")){
   val ids=AndroidSetup.guides(brand,35).map {it.id}
   assertFalse(ids.contains("autostart"));assertFalse(ids.contains("recents"));assertFalse(ids.contains("popup"))
  }
 }
 @Test fun samsungGetsItsOwnOptionalHelp(){assertTrue(AndroidSetup.guides("samsung",35).any {it.id=="background-samsung"})}
 @Test fun restrictedSettingsRequireAndroid13(){assertFalse(AndroidSetup.guides("Google",32).any {it.id=="restricted"});assertTrue(AndroidSetup.guides("Google",33).any {it.id=="restricted"})}
 @Test fun readinessDependsOnRealNotificationConnection(){
  assertEquals(0,AndroidSetup.requiredIssues(true,true))
  assertEquals(1,AndroidSetup.requiredIssues(true,false))
  assertEquals(1,AndroidSetup.requiredIssues(false,false))
  assertEquals(1,AndroidSetup.requiredIssues(false,true))
 }
}
