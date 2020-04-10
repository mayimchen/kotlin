inline class X(val x: Int?)

fun useX(x: X): String = if (x.x == null) "OK" else "fail: $x"

fun <T> call(fn: () -> T) = fn()

fun box() = useX(call { X(null) })