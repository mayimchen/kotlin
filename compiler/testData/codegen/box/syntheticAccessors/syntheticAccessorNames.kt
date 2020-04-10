// TARGET_BACKEND: JVM

// WITH_RUNTIME
// This test checks that synthetic accessors generated by Kotlin compiler have names starting with "access$"
// This is crucial for some JVM frameworks like Quasar which rely on the bytecode being similar to the one generated by javac
// See https://youtrack.jetbrains.com/issue/KT-6870

class PrivatePropertyGet {
    private val x = 42

    inner class Inner { val a = x }
}

class PrivatePropertySet {
    private var x = "a"

    inner class Inner { init { x = "b" } }
}

class PrivateMethod {
    private fun foo() = ""

    inner class Inner { val a = foo() }
}

fun check(klass: Class<*>) {
    for (method in klass.getDeclaredMethods()) {
        if (method.isSynthetic() && method.getName().startsWith("access$")) return
    }

    throw AssertionError("No synthetic methods starting with 'access$' found in class $klass")
}

fun box(): String {
    check(PrivatePropertyGet::class.java)
    check(PrivatePropertySet::class.java)
    check(PrivateMethod::class.java)

    // Also check that synthetic accessors really work
    PrivatePropertyGet().Inner()
    PrivatePropertySet().Inner()
    PrivateMethod().Inner()

    return "OK"
}
