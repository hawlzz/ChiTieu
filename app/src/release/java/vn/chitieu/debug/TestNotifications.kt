package vn.chitieu.debug
import vn.chitieu.notification.ParsedMessage
object TestNotifications {
 fun accepts(pkg:String)=false
 fun parse(pkg:String,body:String,time:Long):ParsedMessage?=null
}
