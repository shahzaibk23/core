package common

import chisel3._
import chisel3.util._ 

object Utils
{
    def copyCommonFields[T <: Record](from: T, to: T): Unit = {
        (to.elements.keySet intersect from.elements.keySet).foreach { key =>
            to.elements(key) := from.elements(key)
        }
    }

}