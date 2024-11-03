package com.emamagic.util;

import javax.lang.model.element.VariableElement;
import java.util.List;

public record PageData(String qualifiedName, List<VariableElement> params) {
}

