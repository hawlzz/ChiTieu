package vn.chitieu.debug
import vn.chitieu.notification.*
import vn.chitieu.domain.*
object TestNotifications {
 @Volatile var enabled=false
 fun accepts(pkg:String)=enabled&&pkg=="com.android.shell"
 fun parse(pkg:String,body:String,time:Long):ParsedMessage?=if(accepts(pkg))SyntheticParser.parse(body.lineSequence().lastOrNull().orEmpty(),time) else null
}
