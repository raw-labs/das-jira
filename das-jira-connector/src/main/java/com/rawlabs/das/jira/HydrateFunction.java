package com.rawlabs.das.jira;

import com.rawlabs.protocol.das.Qual;
import com.rawlabs.protocol.das.SortKey;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

@FunctionalInterface
public interface HydrateFunction<T> {
  Iterator<T> hydrate(
      List<Qual> quals,
      List<String> columns,
      @Nullable List<SortKey> sortKeys,
      @Nullable Long limit);
}
