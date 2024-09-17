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

package com.rawlabs.das.jira

import com.rawlabs.das.sdk.{DASFunction, DASSdk, DASTable}
import com.rawlabs.protocol.das.{FunctionDefinition, TableDefinition}
import com.typesafe.scalalogging.StrictLogging

class DASJira(options: Map[String, String]) extends DASSdk with StrictLogging {

  override def tableDefinitions: Seq[TableDefinition] = ???

  override def functionDefinitions: Seq[FunctionDefinition] = Seq.empty

  override def getTable(name: String): Option[DASTable] = ???

  override def getFunction(name: String): Option[DASFunction] = None
}
