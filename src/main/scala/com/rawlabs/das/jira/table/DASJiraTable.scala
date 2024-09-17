/*
 * Copyright 2024 RAW Labs S.A.
 *
 * Use of this software is governed by the Business Source License
 * included in the file licenses/BSL.txt.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0, included in the file
 * licenses/APL.txt.
 */

package com.rawlabs.das.jira.table

import com.rawlabs.das.sdk.{DASExecuteResult, DASTable}
import com.rawlabs.protocol.das.{Qual, SortKey}
import com.typesafe.scalalogging.StrictLogging

class DASJiraTable extends DASTable with StrictLogging {

  override def getRelSize(quals: Seq[Qual], columns: Seq[String]): (Int, Int) = ???

  override def execute(
      quals: Seq[Qual],
      columns: Seq[String],
      maybeSortKeys: Option[Seq[SortKey]],
      maybeLimit: Option[Long]
  ): DASExecuteResult = ???
}
