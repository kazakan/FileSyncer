package filesyncer.common.argparse

enum class ArgType {
    INT,
    STRING
}

data class ArgData(
    val name: String,
    val type: ArgType,
    val isOptional: Boolean = false,
    val default: Any? = null
)

class ArgParser() {
    var positionalArgData = mutableListOf<ArgData>()
    var optionalArgData = mutableMapOf<String, ArgData>()
    var shortToLong = mutableMapOf<String, String>()
    var argNames = mutableSetOf<String>()

    fun parse(arr: Array<String>): Map<String, Any?> {
        var ret = mutableMapOf<String, Any?>() // <name, value>
        var positionalAdded = 0
        var optionalName: String? = null
        for (token in arr) {
            if (token[0] == '-') {
                if (optionalName != null) throw Exception("Invalid arg format")
                optionalName = shortToLong[token.substring(0, 2)]
            } else if (optionalName == null) {
                // positional arg

                if (positionalArgData.size <= positionalAdded) {
                    throw Exception("Invalid arg format")
                }

                val curPosArg = positionalArgData[positionalAdded]
                ret[curPosArg.name] = tokenToData(curPosArg.type, token)

                ++positionalAdded
            } else {
                val curOptArg = optionalArgData[optionalName]!!
                ret[curOptArg.name] = tokenToData(curOptArg.type, token)
                optionalName = null
            }
        }

        if (positionalAdded < positionalArgData.size) {
            throw Exception("need more positional arg")
        }

        // add optional default values
        for (entry in optionalArgData) {
            if (!ret.contains(entry.key)) ret[entry.key] = entry.value.default
        }

        return ret
    }

    fun addArg(name: String, type: ArgType, optional: Boolean = false, default: Any? = null) {
        if (optional && (default == null)) throw Exception("Optional value need default value")

        val arg = ArgData(name, type, optional, default)

        if (argNames.contains(name)) throw Exception("Duplicate argument named $name")
        argNames.add(name)

        if (optional) {
            val argFlag = '-' + name[0].lowercase()
            if (shortToLong.contains(argFlag)) throw Exception("Duplicate short name")
            shortToLong[argFlag] = name
            optionalArgData[name] = arg
            return
        }

        positionalArgData.add(arg)
    }

    private fun tokenToData(type: ArgType, token: String): Any {
        return when (type) {
            ArgType.INT -> {
                token.toInt()
            }
            ArgType.STRING -> {
                token
            }
        }
    }
}
