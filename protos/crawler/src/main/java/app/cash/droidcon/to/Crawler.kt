package app.cash.droidcon.to

import com.squareup.wire.Message
import com.squareup.wire.WireEnum
import org.reflections.Reflections
import java.io.File
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import kotlin.reflect.full.isSubclassOf

fun main(args: Array<String>) {
  val reflections = Reflections("com.squareup.protos")

  val nodes = mutableSetOf<Class<*>>()
  val edges = mutableMapOf<Class<*>, MutableSet<Class<*>>>()

  val messageSubtypes = reflections.getSubTypesOf(Message::class.java)
  messageSubtypes.forEach {
    nodes += it
  }
  val enumSubtypes = reflections.getSubTypesOf(WireEnum::class.java)
  enumSubtypes.forEach {
    nodes += it
    nodes += it.outerClass() // for DigitalWalletToken and other types whose enclosing class doesn't extend Message
  }

  for (node in nodes) {
    if (node.kotlin.isSubclassOf(WireEnum::class)) {
      edges[node] = mutableSetOf()
      continue
    }

    println("$node:")
    for (it in node.fields) {
      if (Modifier.isStatic(it.modifiers)) {
        continue
      }

      if (it.type == java.util.List::class.java) {
        val listType = it.genericType as ParameterizedType
        val typeArgument = listType.actualTypeArguments[0] as Class<*>
        if (typeArgument.isLeafType()) {
          continue
        }
        // no point.  node field is of type that's an inner class to node
        if (node.outerClass() == typeArgument.outerClass()) {
          continue
        }
        edges.getOrPut(node.outerClass()) { mutableSetOf() }
            .add(typeArgument.outerClass())
        println("  adding field: $it")
        println("  as edge (${node.outerClass()} -> ${typeArgument.outerClass()})")
        continue
      }

      if (it.type.isLeafType()) {
        continue
      }
      // no point.  node field is of type that's an inner class to node
      if (node.outerClass() == it.type.outerClass()) {
        continue
      }
      edges.getOrPut(node.outerClass()) { mutableSetOf() }
          .add(it.type.outerClass())
      println("  adding field: $it")
      println("  as edge (${node.outerClass()} -> ${it.type.outerClass()})")
    }
  }

  println("START topological sort...")

  // quick check that only enclosing classes exist in edges
  edges.keys.forEach { it.outerClass() == it }
  edges.values.forEach { neighbors -> neighbors.forEach { it.outerClass() == it } }

  // List where we'll be storing the topological order
  val order = mutableListOf<Class<*>>()

  // Map which indicates if a node is visited (has been processed by the algorithm)
  val visited = mutableMapOf<Class<*>, Boolean>()
  for (node in nodes) {
    visited[node] = false
  }

  for (node in edges.keys) {
//  for (node in nodes) {
    if (!visited.getValue(node)) {
      blackMagic(node, visited, order, edges)
    }
  }

  File("../crawler_out").printWriter()
      .use { out ->
        order.forEach { clazz ->
          out.println(clazz.name)
        }
      }

  println("END topological sort...")
}

fun blackMagic(
  v: Class<*>,
  visited: MutableMap<Class<*>, Boolean>,
  order: MutableList<Class<*>>,
  edges: MutableMap<Class<*>, MutableSet<Class<*>>>
) {
  // Mark the current node as visited
  visited[v] = true

  // We reuse the algorithm on all adjacent nodes to the current node
  for (neighbor in (edges[v] ?: emptySet<Class<*>>())) {
//    if (visiting.getValue(neighbor)) {
//      // cycle detected, everything from neighbor to v must be compiled at same time
//
//    }
    if (!visited.getValue(neighbor)) {
      blackMagic(neighbor, visited, order, edges)
    }
  }

  // Put the current node in the array
  order += v
}

fun Class<*>.isLeafType(): Boolean {
  return isPrimitive ||
      isPrimitiveWrapper() ||
      kotlin.isSubclassOf(String::class) ||
      !`package`.name.startsWith("com.squareup.protos")
}

fun Class<*>.outerClass(): Class<*> {
  var outerClass = this
  while (outerClass.enclosingClass != null) outerClass = outerClass.enclosingClass
  return outerClass
}

fun Class<*>.isPrimitiveWrapper(): Boolean {
  return this === java.lang.Double::class.java ||
      this === java.lang.Float::class.java ||
      this === java.lang.Long::class.java ||
      this === java.lang.Integer::class.java ||
      this === java.lang.Short::class.java ||
      this === java.lang.Character::class.java ||
      this === java.lang.Byte::class.java ||
      this === java.lang.Boolean::class.java
}