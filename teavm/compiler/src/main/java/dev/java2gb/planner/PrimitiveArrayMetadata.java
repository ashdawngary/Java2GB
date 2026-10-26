package dev.java2gb.planner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import org.teavm.backend.lowlevel.generate.ClassGeneratorUtil;
import org.teavm.backend.lowlevel.generate.NameProvider;
import org.teavm.model.ValueType;

final class PrimitiveArrayMetadata {
  private PrimitiveArrayMetadata() {}

  static void emit(Path output, AllocationPlanner planner, NameProvider names) throws IOException {
    var types = new LinkedHashSet<ValueType.Array>();
    for (var site : planner.sites) {
      if (site.type instanceof ValueType.Array array) {
        types.add(array);
      }
    }
    for (var array : types) {
      ValueType item = array.getItemType();
      String file, cType;
      if (item.equals(ValueType.BOOLEAN)) {
        file = "boolean";
        cType = "int8_t";
      } else if (item.equals(ValueType.BYTE)) {
        file = "byte";
        cType = "int8_t";
      } else if (item.equals(ValueType.SHORT)) {
        file = "short";
        cType = "int16_t";
      } else if (item.equals(ValueType.CHARACTER)) {
        file = "char";
        cType = "char16_t";
      } else {
        file = "int";
        cType = "int32_t";
      }
      String primitive = names.forClassInstance(item);
      String arrayClass = names.forClassInstance(array);
      String primitiveSuper = names.forSupertypeFunction(item);
      String arraySuper = names.forSupertypeFunction(array);
      Path primitiveSource = output.resolve("primitives/" + file + ".c");
      Path arraySource = output.resolve("arrays/primitives/" + file + ".c");
      Files.createDirectories(primitiveSource.getParent());
      Files.createDirectories(arraySource.getParent());
      if (!Files.exists(primitiveSource)) {
        Files.writeString(
            output.resolve("primitives/" + file + ".h"),
            "#pragma once\n#include \"../runtime.h\"\nextern TeaVM_Class " + primitive + ";\n");
        Files.writeString(
            primitiveSource,
            "#include \""
                + file
                + ".h\"\n"
                + "#include \"../arrays/primitives/"
                + file
                + ".h\"\n"
                + "int32_t "
                + primitiveSuper
                + "(TeaVM_Class* parent, TeaVM_Class* cls) { return 0; }\n"
                + "TeaVM_Class "
                + primitive
                + " = { .size = sizeof("
                + cType
                + "), .flags = "
                + ClassGeneratorUtil.applyPrimitiveFlags(0, item)
                + ", .tag = INT32_MAX, .arrayType = (TeaVM_Class*)&"
                + arrayClass
                + ", .isSupertypeOf = &"
                + primitiveSuper
                + " };\n");
      }
      if (!Files.exists(arraySource)) {
        Files.writeString(
            output.resolve("arrays/primitives/" + file + ".h"),
            "#pragma once\n#include \"../../runtime.h\"\n"
                + "#include \"../../classes/java/lang/Object.h\"\nextern jl_Object_VT "
                + arrayClass
                + ";\n");
        Files.writeString(
            arraySource,
            "#include \""
                + file
                + ".h\"\n"
                + "#include \"../../primitives/"
                + file
                + ".h\"\n"
                + "int32_t "
                + arraySuper
                + "(TeaVM_Class* parent, TeaVM_Class* cls) {\n"
                + " return cls->itemType == &"
                + primitive
                + ";\n}\n"
                + "jl_Object_VT "
                + arrayClass
                + " = { .parent = { .size = sizeof("
                + cType
                + "),"
                + " .itemType = &"
                + primitive
                + ", .isSupertypeOf = &"
                + arraySuper
                + ", .superclass = (TeaVM_Class*)&jl_Object_Cls } };\n");
      }
    }
  }
}
