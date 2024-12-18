package com.rawlabs.das.jira.tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rawlabs.protocol.das.v1.query.*;
import com.rawlabs.protocol.das.v1.types.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/** Comprehensive coverage for Qual across all columns that DASJiraJqlQueryBuilder supports. */
@DisplayName("DASJiraJqlQueryBuilder - All SimpleQual Tests")
public class DASJiraJqlQueryBuilderTest {

  private final DASJiraJqlQueryBuilder jqlBuilder =
      new DASJiraJqlQueryBuilder(ZoneId.of("UTC"), ZoneId.of("UTC"));

  // ------------------------------------------------------------------------
  //  Helper methods
  // ------------------------------------------------------------------------

  private Value stringValue(String s) {
    return Value.newBuilder().setString(ValueString.newBuilder().setV(s)).build();
  }

  @Test
  @DisplayName("Should map operators to JQL")
  public void shouldMapOperators() {
    var queryBuilder = new DASJiraJqlQueryBuilder(null, null);
    var geq =
        queryBuilder.mapOperator(Operator.GREATER_THAN_OR_EQUAL);

    var eq = queryBuilder.mapOperator(Operator.EQUALS);

    assertEquals(">=", geq);
    assertEquals("=", eq);
}

  private Value nullValue() {
    return Value.newBuilder().setNull(ValueNull.getDefaultInstance()).build();
  }

  private Value timestampValue(int year, int month, int day, int hour, int min, int sec, int nano) {
    return Value.newBuilder()
        .setTimestamp(
            ValueTimestamp.newBuilder()
                .setYear(year)
                .setMonth(month)
                .setDay(day)
                .setHour(hour)
                .setMinute(min)
                .setSecond(sec)
                .setNano(nano))
        .build();
  }

  private Value dateValue(int year, int month, int day) {
    return Value.newBuilder()
        .setDate(ValueDate.newBuilder().setYear(year).setMonth(month).setDay(day))
        .build();
  }

  private Qual simpleQual(String column, Operator op, Value val) {
    return Qual.newBuilder()
        .setName(column)
        .setSimpleQual(SimpleQual.newBuilder().setOperator(op).setValue(val).build())
        .build();
  }

  /**
   * Creates a ListQual for (column operator [values]) with isAny controlling IN or NOT IN usage: -
   * eq + isAny=true => IN(...) - ne + isAny=false => NOT IN(...)
   */
  private Qual listQual(String column, Operator op, boolean isAny, List<Value> values) {
    Qual.Builder builder = Qual.newBuilder().setName(column);
    if (isAny) {
      builder.setIsAnyQual(IsAnyQual.newBuilder().setOperator(op).addAllValues(values));
    } else {
      builder.setIsAllQual(IsAllQual.newBuilder().setOperator(op).addAllValues(values));
    }
    return builder.build();
  }

  // --------------------------------------------------------------------------
  // Columns: "id", "key", "title" => all turn into JQL field "issueKey"
  //    - All comparisons are supported: =, !=, <, <=, >, >=
  //    - Null is not supported => mkJql returns Optional.empty()
  //    - All values are quoted except for numeric values
  //    - IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName(
      "Columns: id/key/title => issueKey (All comparisons supported, Null not supported, IN supported)")
  class IssueKeyTests {

    @Test
    @DisplayName("issueKey = 'ABC-123' => issueKey = \"ABC-123\"")
    void testEq() {
      Qual q = simpleQual("key", Operator.EQUALS, stringValue("ABC-123"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("issueKey = \"ABC-123\"", res.orElseThrow());
    }

    @Test
    @DisplayName("issueKey != 'XYZ-999' => issueKey != \"XYZ-999\"")
    void testNe() {
      Qual q = simpleQual("id", Operator.NOT_EQUALS, stringValue("XYZ-999"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("issueKey != \"XYZ-999\"", res.orElseThrow());
    }

    @Test
    @DisplayName("issueKey < 100 => issueKey < 100")
    void testLt() {
      Qual q = simpleQual("title", Operator.LESS_THAN, stringValue("100"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("issueKey < 100", res.orElseThrow());
    }

    @Test
    @DisplayName("issueKey <= 'ABC-1' => issueKey <= \"ABC-1\"")
    void testLte() {
      Qual q = simpleQual("key", Operator.LESS_THAN_OR_EQUAL, stringValue("ABC-1"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("issueKey <= \"ABC-1\"", res.orElseThrow());
    }

    @Test
    @DisplayName("issueKey > 123 => issueKey > 123 (unquoted if numeric)")
    void testGt() {
      Qual q = simpleQual("id", Operator.GREATER_THAN, stringValue("123"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("issueKey > 123", res.orElseThrow());
    }

    @Test
    @DisplayName("issueKey >= \"XYZ-123\" => issueKey >= \"XYZ-123\"")
    void testGte() {
      Qual q = simpleQual("title", Operator.GREATER_THAN_OR_EQUAL, stringValue("XYZ-123"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("issueKey >= \"XYZ-123\"", res.orElseThrow());
    }

    @Test
    @DisplayName("issueKey = null => not supported => Optional.empty()")
    void testNull() {
      Qual q = simpleQual("key", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty(), "Should be Optional.empty() because null is not supported for key");
    }

    @Test
    @DisplayName("issueKey IN(...) and NOT IN(...)")
    public void testIssueKeyInNotIn() {
      // eq + isAny = true => issueKey IN (...)
      Qual inQual =
          listQual(
              "key", Operator.EQUALS, true, List.of(stringValue("ABC-123"), stringValue("XYZ-888")));
      // ne + isAny = false => issueKey NOT IN (...)
      Qual notInQual =
          listQual(
              "title", Operator.NOT_EQUALS, false, List.of(stringValue("K-999"), stringValue("T-111")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("issueKey IN (\"ABC-123\", \"XYZ-888\")", jqls.get(0).orElseThrow());
      assertEquals("issueKey NOT IN (\"K-999\", \"T-111\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Columns: "project_key", "project_id", "project_name" => "project"
  //    - Supported: =, !=, plus null checks => IS NULL, IS NOT NULL
  //    - Not supported: <, <=, >, >= => Optional.empty()
  //    - IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName(
      "Columns: project_key/id/name => project (==, !=, NULL checks supported, IN supported)")
  class ProjectTests {

    @Test
    @DisplayName("project_key = 'ABC' => project = \"ABC\" (or unquoted if numeric)")
    void testEq() {
      Qual q = simpleQual("project_key", Operator.EQUALS, stringValue("ABC"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("project = \"ABC\"", res.orElseThrow());
    }

    @Test
    @DisplayName("project_id != 123 => project != 123")
    void testNe() {
      Qual q = simpleQual("project_id", Operator.NOT_EQUALS, stringValue("123"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("project != 123", res.orElseThrow());
    }

    @Test
    @DisplayName("project_name = null => project IS NULL")
    void testEqNull() {
      Qual q = simpleQual("project_name", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("project IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("project_key != null => project IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("project_key", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("project IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("project_id < 'ABC' => not supported => Optional.empty()")
    void testLt() {
      Qual q = simpleQual("project_id", Operator.LESS_THAN, stringValue("ABC"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("project IN(...) and NOT IN(...)")
    public void testProjectInNotIn() {
      // eq + isAny => project IN(...)
      Qual inQual =
          listQual(
              "project_key",
              Operator.EQUALS,
              true,
              List.of(stringValue("MYPROJ"), stringValue("1234")));
      // ne + !isAny => project NOT IN(...)
      Qual notInQual =
          listQual(
              "project_id", Operator.NOT_EQUALS, false, List.of(stringValue("X"), stringValue("999")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("project IN (\"MYPROJ\", 1234)", jqls.get(0).orElseThrow());
      assertEquals("project NOT IN (\"X\", 999)", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: "status"
  //    - eq, ne supported, plus null checks => IS NULL, IS NOT NULL
  //    - <, <=, >, >= => not supported => Optional.empty()
  //    - IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: status => eq, ne, null checks only, IN supported")
  class StatusTests {

    @Test
    @DisplayName("status = 'Open' => status = \"Open\"")
    void testEq() {
      Qual q = simpleQual("status", Operator.EQUALS, stringValue("Open"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("status = \"Open\"", res.orElseThrow());
    }

    @Test
    @DisplayName("status != 'Closed' => status != \"Closed\"")
    void testNe() {
      Qual q = simpleQual("status", Operator.NOT_EQUALS, stringValue("Closed"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("status != \"Closed\"", res.orElseThrow());
    }

    @Test
    @DisplayName("status = null => status IS NULL")
    void testEqNull() {
      Qual q = simpleQual("status", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("status IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("status != null => status IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("status", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("status IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("status < 'X' => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("status", Operator.LESS_THAN, stringValue("X"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("status IN(...) and NOT IN(...)")
    public void testStatusInNotIn() {
      // eq + isAny => status IN(...)
      Qual inQual =
          listQual(
              "status",
              Operator.EQUALS,
              true,
              List.of(stringValue("Open"), stringValue("In Progress")));

      // ne + !isAny => status NOT IN(...)
      Qual notInQual =
          listQual(
              "status",
              Operator.NOT_EQUALS,
              false,
              List.of(stringValue("Closed"), stringValue("Resolved")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("status IN (\"Open\", \"In Progress\")", jqls.get(0).orElseThrow());
      assertEquals("status NOT IN (\"Closed\", \"Resolved\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: "status_category" => statusCategory
  //    - eq, ne, null checks supported
  //    - <, <=, >, >= => not supported
  //    - IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: status_category => eq, ne, null checks only, IN supported")
  class StatusCategoryTests {

    @Test
    @DisplayName("status_category = 'To Do' => statusCategory = \"To Do\"")
    void testEq() {
      Qual q = simpleQual("status_category", Operator.EQUALS, stringValue("To Do"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("statusCategory = \"To Do\"", res.orElseThrow());
    }

    @Test
    @DisplayName("status_category != 'Done' => statusCategory != \"Done\"")
    void testNe() {
      Qual q = simpleQual("status_category", Operator.NOT_EQUALS, stringValue("Done"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("statusCategory != \"Done\"", res.orElseThrow());
    }

    @Test
    @DisplayName("status_category = null => statusCategory IS NULL")
    void testEqNull() {
      Qual q = simpleQual("status_category", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("statusCategory IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("status_category != null => statusCategory IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("status_category", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("statusCategory IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("status_category < 'X' => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("status_category", Operator.LESS_THAN, stringValue("X"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("statusCategory IN(...) and NOT IN(...)")
    public void testStatusCategoryInNotIn() {
      Qual inQual =
          listQual(
              "status_category",
              Operator.EQUALS,
              true,
              List.of(stringValue("To Do"), stringValue("In Progress")));
      Qual notInQual =
          listQual(
              "status_category",
              Operator.NOT_EQUALS,
              false,
              List.of(stringValue("Done"), stringValue("Closed")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("statusCategory IN (\"To Do\", \"In Progress\")", jqls.get(0).orElseThrow());
      assertEquals("statusCategory NOT IN (\"Done\", \"Closed\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: "epic_key" => parent
  //    - eq, ne, null checks => supported
  //    - <, <=, >, >= => not supported
  //    - IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: epic_key => parent => eq, ne, null checks")
  class EpicKeyTests {

    @Test
    @DisplayName("epic_key = 'ABC-123' => parent = \"ABC-123\"")
    void testEq() {
      Qual q = simpleQual("epic_key", Operator.EQUALS, stringValue("ABC-123"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("parent = \"ABC-123\"", res.orElseThrow());
    }

    @Test
    @DisplayName("epic_key != 'XYZ' => parent != \"XYZ\"")
    void testNe() {
      Qual q = simpleQual("epic_key", Operator.NOT_EQUALS, stringValue("XYZ"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("parent != \"XYZ\"", res.orElseThrow());
    }

    @Test
    @DisplayName("epic_key = null => parent IS NULL")
    void testEqNull() {
      Qual q = simpleQual("epic_key", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("parent IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("epic_key != null => parent IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("epic_key", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("parent IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("epic_key < 'xx' => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("epic_key", Operator.LESS_THAN, stringValue("xx"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("epic_key IN(...) and NOT IN(...) => parent IN/NOT IN(...)")
    public void testEpicKeyInNotIn() {
      Qual inQual =
          listQual("epic_key", Operator.EQUALS, true, List.of(stringValue("E-1"), stringValue("E-2")));
      Qual notInQual =
          listQual(
              "epic_key", Operator.NOT_EQUALS, false, List.of(stringValue("E-99"), stringValue("E-100")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("parent IN (\"E-1\", \"E-2\")", jqls.get(0).orElseThrow());
      assertEquals("parent NOT IN (\"E-99\", \"E-100\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Columns: assignee_* => "assignee"
  //    eq, ne, null checks supported
  //    <, <=, >, >= => not supported
  //    IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Columns: assignee_* => assignee => eq, ne, null checks only, IN supported")
  class AssigneeTests {

    @Test
    @DisplayName("assignee_email_address = 'bob@example.com' => assignee = \"bob@example.com\"")
    void testEq() {
      Qual q = simpleQual("assignee_email_address", Operator.EQUALS, stringValue("bob@example.com"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("assignee = \"bob@example.com\"", res.orElseThrow());
    }

    @Test
    @DisplayName("assignee_account_id != '1234' => assignee != 1234")
    void testNe() {
      Qual q = simpleQual("assignee_account_id", Operator.NOT_EQUALS, stringValue("1234"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("assignee != 1234", res.orElseThrow());
    }

    @Test
    @DisplayName("assignee_display_name = null => assignee IS NULL")
    void testEqNull() {
      Qual q = simpleQual("assignee_display_name", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("assignee IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("assignee_account_id != null => assignee IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("assignee_account_id", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("assignee IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("assignee_* < => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("assignee_email_address", Operator.LESS_THAN, stringValue("zzz"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("assignee IN(...) and NOT IN(...)")
    public void testAssigneeInNotIn() {
      Qual inQual =
          listQual(
              "assignee_display_name",
              Operator.EQUALS,
              true,
              List.of(stringValue("Alice"), stringValue("Bob")));
      Qual notInQual =
          listQual(
              "assignee_email_address",
              Operator.NOT_EQUALS,
              false,
              List.of(stringValue("charlie@example.com"), stringValue("david@example.com")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("assignee IN (\"Alice\", \"Bob\")", jqls.get(0).orElseThrow());
      assertEquals(
          "assignee NOT IN (\"charlie@example.com\", \"david@example.com\")",
          jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Columns: creator_* => "creator"
  //    eq, ne, null checks supported
  //    <, <=, >, >= => not supported
  //    IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Columns: creator_* => creator => eq, ne, null checks only, IN supported")
  class CreatorTests {

    @Test
    @DisplayName("creator_email_address = 'alice@raw-labs.com' => creator = \"alice@raw-labs.com\"")
    void testEq() {
      Qual q = simpleQual("creator_email_address", Operator.EQUALS, stringValue("alice@raw-labs.com"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("creator = \"alice@raw-labs.com\"", res.orElseThrow());
    }

    @Test
    @DisplayName("creator_account_id != 'abcd' => creator != \"abcd\" (or numeric unquoted)")
    void testNe() {
      Qual q = simpleQual("creator_account_id", Operator.NOT_EQUALS, stringValue("abcd"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("creator != \"abcd\"", res.orElseThrow());
    }

    @Test
    @DisplayName("creator_display_name = null => creator IS NULL")
    void testEqNull() {
      Qual q = simpleQual("creator_display_name", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("creator IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("creator_email_address != null => creator IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("creator_email_address", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("creator IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("creator_* < => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("creator_account_id", Operator.LESS_THAN, stringValue("1234"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("creator IN(...) and NOT IN(...)")
    public void testCreatorInNotIn() {
      Qual inQual =
          listQual(
              "creator_email_address",
              Operator.EQUALS,
              true,
              List.of(stringValue("alice@foo.com"), stringValue("bob@foo.com")));
      Qual notInQual =
          listQual(
              "creator_account_id",
              Operator.NOT_EQUALS,
              false,
              List.of(stringValue("1234"), stringValue("5678")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("creator IN (\"alice@foo.com\", \"bob@foo.com\")", jqls.get(0).orElseThrow());
      // numeric => unquoted
      assertEquals("creator NOT IN (1234, 5678)", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Columns: reporter_* => "reporter"
  //    eq, ne, null checks supported
  //    <, <=, >, >= => not supported
  //    IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Columns: reporter_* => reporter => eq, ne, null checks only, IN supported")
  class ReporterTests {

    @Test
    @DisplayName("reporter_display_name = 'John Doe' => reporter = \"John Doe\"")
    void testEq() {
      Qual q = simpleQual("reporter_display_name", Operator.EQUALS, stringValue("John Doe"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("reporter = \"John Doe\"", res.orElseThrow());
    }

    @Test
    @DisplayName("reporter_display_name != 'John Doe' => reporter != \"John Doe\"")
    void testNe() {
      Qual q = simpleQual("reporter_display_name", Operator.NOT_EQUALS, stringValue("John Doe"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("reporter != \"John Doe\"", res.orElseThrow());
    }

    @Test
    @DisplayName("reporter_account_id = null => reporter IS NULL")
    void testEqNull() {
      Qual q = simpleQual("reporter_account_id", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("reporter IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("reporter_display_name != null => reporter IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("reporter_display_name", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("reporter IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("reporter_* < => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("reporter_display_name", Operator.LESS_THAN, stringValue("Z"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("reporter IN(...) and NOT IN(...)")
    public void testReporterInNotIn() {
      Qual inQual =
          listQual(
              "reporter_display_name",
              Operator.EQUALS,
              true,
              List.of(stringValue("Tom"), stringValue("Jerry")));
      Qual notInQual =
          listQual(
              "reporter_account_id",
              Operator.NOT_EQUALS,
              false,
              List.of(stringValue("1234"), stringValue("1235")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("reporter IN (\"Tom\", \"Jerry\")", jqls.get(0).orElseThrow());
      assertEquals("reporter NOT IN (1234, 1235)", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: created => "created"
  //    - All comparisons supported: =, !=, <, <=, >, >=
  //    - Null => created IS NULL, created IS NOT NULL
  //    - IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: created => all comparisons, null checks")
  class CreatedTests {

    @Test
    @DisplayName("created = '2024-01-01T10:00:00Z' => created = \"2024-01-01 10:00\"")
    void testEq() {
      Qual q = simpleQual("created", Operator.EQUALS, timestampValue(2024, 1, 1, 10, 0, 0, 0));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created = \"2024-01-01 10:00\"", res.orElseThrow());
    }

    @Test
    @DisplayName("created != '2024-02-02' => created != \"2024-02-02\" (Date => yyyy-MM-dd)")
    void testNe() {
      Qual q = simpleQual("created", Operator.NOT_EQUALS, dateValue(2024, 2, 2));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created != \"2024-02-02\"", res.orElseThrow());
    }

    @Test
    @DisplayName("created < '2023-12-31T23:00' => created < \"2023-12-31 23:00\"")
    void testLt() {
      Qual q = simpleQual("created", Operator.LESS_THAN, timestampValue(2023, 12, 31, 23, 0, 0, 0));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created < \"2023-12-31 23:00\"", res.orElseThrow());
    }

    @Test
    @DisplayName("created <= '2024-05-01T10:00' => created <= \"2024-05-01 10:00\"")
    void testLte() {
      Qual q = simpleQual("created", Operator.LESS_THAN_OR_EQUAL, timestampValue(2024, 5, 1, 10, 0, 0, 0));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created <= \"2024-05-01 10:00\"", res.orElseThrow());
    }

    @Test
    @DisplayName("created > '2023-10-10T15:30' => created > \"2023-10-10 15:30\"")
    void testGt() {
      Qual q = simpleQual("created", Operator.GREATER_THAN, timestampValue(2023, 10, 10, 15, 30, 0, 0));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created > \"2023-10-10 15:30\"", res.orElseThrow());
    }

    @Test
    @DisplayName("created >= '2023-01-01' => created >= \"2023-01-01\"")
    void testGte() {
      Qual q = simpleQual("created", Operator.GREATER_THAN_OR_EQUAL, dateValue(2023, 1, 1));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created >= \"2023-01-01\"", res.orElseThrow());
    }

    @Test
    @DisplayName("created = null => created IS NULL")
    void testEqNull() {
      Qual q = simpleQual("created", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("created != null => created IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("created", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("created IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("created IN(...) and NOT IN(...) for timestamps/dates")
    public void testCreatedInNotIn() {
      // eq + isAny => created IN(...)
      // For demonstration, mix a timestampValue and a dateValue
      Qual inQual =
          listQual(
              "created",
              Operator.EQUALS,
              true,
              List.of(
                  timestampValue(2024, 1, 1, 10, 0, 0, 0),
                  dateValue(2024, 2, 5) // code will produce "2024-02-05"
                  ));
      // ne + !isAny => created NOT IN(...)
      Qual notInQual =
          listQual(
              "created",
              Operator.NOT_EQUALS,
              false,
              List.of(
                  timestampValue(2023, 12, 31, 23, 0, 0, 0),
                  timestampValue(2024, 6, 10, 12, 30, 0, 0)));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      // eq + isAny => created IN("2024-01-01 10:00", "2024-02-05")
      assertEquals("created IN (\"2024-01-01 10:00\", \"2024-02-05\")", jqls.get(0).orElseThrow());
      // ne + !isAny => created NOT IN("2023-12-31 23:00", "2024-06-10 12:30")
      assertEquals(
          "created NOT IN (\"2023-12-31 23:00\", \"2024-06-10 12:30\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: due_date => "due"
  //     - All comparisons, plus null checks
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: due_date => due => all comparisons, null checks")
  class DueDateTests {

    @Test
    @DisplayName("due_date = null => due IS NULL")
    void testEqNull() {
      Qual q = simpleQual("due_date", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("due IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("due_date != null => due IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("due_date", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("due IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("due_date > '2024-10-01T10:00' => due > \"2024-10-01 10:00\"")
    void testGt() {
      Qual q = simpleQual("due_date", Operator.GREATER_THAN, timestampValue(2024, 10, 1, 10, 0, 0, 0));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("due > \"2024-10-01 10:00\"", res.orElseThrow());
    }

    @Test
    @DisplayName("due_date IN(...) / NOT IN(...)")
    public void testDueDateInNotIn() {
      Qual inQual =
          listQual(
              "due_date",
              Operator.EQUALS,
              true,
              List.of(dateValue(2025, 3, 10), timestampValue(2025, 3, 11, 8, 0, 0, 0)));
      Qual notInQual =
          listQual(
              "due_date",
              Operator.NOT_EQUALS,
              false,
              List.of(
                  timestampValue(2025, 1, 1, 12, 0, 0, 0),
                  timestampValue(2025, 1, 2, 12, 0, 0, 0)));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("due IN (\"2025-03-10\", \"2025-03-11 08:00\")", jqls.get(0).orElseThrow());
      assertEquals(
          "due NOT IN (\"2025-01-01 12:00\", \"2025-01-02 12:00\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: due_date => "due"
  //     - All comparisons, plus null checks
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: updated => updated => all comparisons, null checks")
  class UpdatedDateTests {

    @Test
    @DisplayName("updated = null => updated IS NULL")
    void testEqNull() {
      Qual q = simpleQual("updated", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("updated IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("updated != null => updated IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("updated", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("updated IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("updated > '2024-10-01T10:00' => updated > \"2024-10-01 10:00\"")
    void testGt() {
      Qual q = simpleQual("updated", Operator.GREATER_THAN, timestampValue(2024, 10, 1, 10, 0, 0, 0));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("updated > \"2024-10-01 10:00\"", res.orElseThrow());
    }

    @Test
    @DisplayName("updated IN(...) / NOT IN(...)")
    public void testupdatedDateInNotIn() {
      Qual inQual =
          listQual(
              "updated",
              Operator.EQUALS,
              true,
              List.of(dateValue(2025, 3, 10), timestampValue(2025, 3, 11, 8, 0, 0, 0)));
      Qual notInQual =
          listQual(
              "updated",
              Operator.NOT_EQUALS,
              false,
              List.of(
                  timestampValue(2025, 1, 1, 12, 0, 0, 0),
                  timestampValue(2025, 1, 2, 12, 0, 0, 0)));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("updated IN (\"2025-03-10\", \"2025-03-11 08:00\")", jqls.get(0).orElseThrow());
      assertEquals(
          "updated NOT IN (\"2025-01-01 12:00\", \"2025-01-02 12:00\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: resolution_date => "resolved"
  //     - All comparisons, plus null checks
  //     - IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: resolution_date => resolved => all comparisons, null checks")
  class ResolutionDateTests {
    @Test
    @DisplayName("resolution_date = 2023-01-10T12:00 => resolved = \"2023-01-10 12:00\"")
    void testEq() {
      Qual q =
          simpleQual("resolution_date", Operator.EQUALS, timestampValue(2023, 1, 10, 12, 0, 0, 0));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("resolved = \"2023-01-10 12:00\"", res.orElseThrow());
    }

    @Test
    @DisplayName("resolution_date = null => resolved IS NULL")
    void testEqNull() {
      Qual q = simpleQual("resolution_date", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("resolved IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("resolution_date => resolved IN(...) / NOT IN(...)")
    public void testResolutionDateInNotIn() {
      // eq + isAny => resolved IN(...)
      Qual inQual =
          listQual(
              "resolution_date",
              Operator.EQUALS,
              true,
              List.of(
                  timestampValue(2024, 1, 1, 10, 0, 0, 0),
                  timestampValue(2024, 2, 10, 15, 30, 0, 0)));
      // ne + !isAny => resolved NOT IN(...)
      Qual notInQual =
          listQual(
              "resolution_date",
              Operator.NOT_EQUALS,
              false,
              List.of(dateValue(2024, 3, 1), timestampValue(2024, 4, 15, 9, 45, 0, 0)));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals(
          "resolved IN (\"2024-01-01 10:00\", \"2024-02-10 15:30\")", jqls.get(0).orElseThrow());
      assertEquals(
          "resolved NOT IN (\"2024-03-01\", \"2024-04-15 09:45\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: summary => special eq => summary ~, ne => summary !~, null => IS/IS NOT NULL
  //     - No <, <=, >, >=
  //     - = is supported using ~ (contains) as an approximation
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: summary => eq => summary ~, ne => summary !~, null checks, others => empty")
  class SummaryTests {
    @Test
    @DisplayName("summary = 'foo' => summary ~ \"foo\"")
    void testEq() {
      Qual q = simpleQual("summary", Operator.EQUALS, stringValue("foo"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("summary ~ \"foo\"", res.orElseThrow());
    }

    @Test
    @DisplayName("summary != 'bar' => summary !~ \"bar\"")
    void testNe() {
      Qual q = simpleQual("summary", Operator.NOT_EQUALS, stringValue("bar"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("summary !~ \"bar\"", res.orElseThrow());
    }

    @Test
    @DisplayName("summary = null => summary IS NULL")
    void testEqNull() {
      Qual q = simpleQual("summary", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("summary IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("summary != null => summary IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("summary", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("summary IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("summary < 'x' => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("summary", Operator.LESS_THAN, stringValue("x"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("summary IN(...) / NOT IN(...) not supported")
    public void testSummaryInNotIn() {
      // eq + isAny => resolved IN(...)
      Qual inQual =
          listQual("summary", Operator.EQUALS, true, List.of(stringValue("foo"), stringValue("bar")));
      // ne + !isAny => resolved NOT IN(...)
      Qual notInQual =
          listQual("summary", Operator.NOT_EQUALS, false, List.of(stringValue("baz"), stringValue("qux")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assert (jqls.get(0).isEmpty());
      assert (jqls.get(1).isEmpty());
    }
  }

  // --------------------------------------------------------------------------
  // Column: type => eq, ne, null checks. < etc. => not supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: type => eq, ne, null checks only")
  class TypeTests {
    @Test
    @DisplayName("type = 'Bug' => type = \"Bug\"")
    void testEq() {
      Qual q = simpleQual("type", Operator.EQUALS, stringValue("Bug"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("type = \"Bug\"", res.orElseThrow());
    }

    @Test
    @DisplayName("type != 'Task' => type != \"Task\"")
    void testNe() {
      Qual q = simpleQual("type", Operator.NOT_EQUALS, stringValue("Task"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("type != \"Task\"", res.orElseThrow());
    }

    @Test
    @DisplayName("type = null => type IS NULL")
    void testEqNull() {
      Qual q = simpleQual("type", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("type IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("type != null => type IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("type", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("type IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("type < => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("type", Operator.LESS_THAN, stringValue("X"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("type IN(...) and NOT IN(...)")
    public void testTypeInNotIn() {
      Qual inQual =
          listQual("type", Operator.EQUALS, true, List.of(stringValue("Bug"), stringValue("Task")));
      Qual notInQual =
          listQual("type", Operator.NOT_EQUALS, false, List.of(stringValue("Story"), stringValue("Epic")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("type IN (\"Bug\", \"Task\")", jqls.get(0).orElseThrow());
      assertEquals("type NOT IN (\"Story\", \"Epic\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Column: priority => eq, ne, null checks only
  // We don't support > and < because the priority values are not ordered like strings
  // IN supported
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Column: priority => eq, ne, null checks only")
  class PriorityTests {
    @Test
    @DisplayName("priority = 'High' => priority = \"High\"")
    void testEq() {
      Qual q = simpleQual("priority", Operator.EQUALS, stringValue("High"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("priority = \"High\"", res.orElseThrow());
    }

    @Test
    @DisplayName("priority != 'Low' => priority != \"Low\"")
    void testNe() {
      Qual q = simpleQual("priority", Operator.NOT_EQUALS, stringValue("Low"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("priority != \"Low\"", res.orElseThrow());
    }

    @Test
    @DisplayName("priority = null => priority IS NULL")
    void testEqNull() {
      Qual q = simpleQual("priority", Operator.EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("priority IS NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("priority != null => priority IS NOT NULL")
    void testNeNull() {
      Qual q = simpleQual("priority", Operator.NOT_EQUALS, nullValue());
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertEquals("priority IS NOT NULL", res.orElseThrow());
    }

    @Test
    @DisplayName("priority < => not supported => Optional.empty()")
    void testUnsupportedLt() {
      Qual q = simpleQual("priority", Operator.LESS_THAN, stringValue("X"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("priority IN(...) and NOT IN(...)")
    public void testPriorityInNotIn() {
      Qual inQual =
          listQual(
              "priority", Operator.EQUALS, true, List.of(stringValue("High"), stringValue("Medium")));
      Qual notInQual =
          listQual(
              "priority",
              Operator.NOT_EQUALS,
              false,
              List.of(stringValue("Low"), stringValue("Critical")));

      var jqls = jqlBuilder.mkJql(List.of(inQual, notInQual));
      assertEquals("priority IN (\"High\", \"Medium\")", jqls.get(0).orElseThrow());
      assertEquals("priority NOT IN (\"Low\", \"Critical\")", jqls.get(1).orElseThrow());
    }
  }

  // --------------------------------------------------------------------------
  // Columns: labels, components, fields, tags, sprint_ids, sprint_names, description, self
  //     => not supported => always Optional.empty()
  // --------------------------------------------------------------------------
  @Nested
  @DisplayName("Unsupported columns => always Optional.empty()")
  class UnsupportedColumnsTests {

    @Test
    @DisplayName("labels => Optional.empty()")
    void testLabels() {
      Qual q = simpleQual("labels", Operator.EQUALS, stringValue("something"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("components => Optional.empty()")
    void testComponents() {
      Qual q = simpleQual("components", Operator.EQUALS, stringValue("UI"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("fields => Optional.empty()")
    void testFields() {
      Qual q = simpleQual("fields", Operator.EQUALS, stringValue("anything"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("tags => Optional.empty()")
    void testTags() {
      Qual q = simpleQual("tags", Operator.EQUALS, stringValue("myTag"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("sprint_ids => Optional.empty()")
    void testSprintIds() {
      Qual q = simpleQual("sprint_ids", Operator.EQUALS, stringValue("123"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("sprint_names => Optional.empty()")
    void testSprintNames() {
      Qual q = simpleQual("sprint_names", Operator.EQUALS, stringValue("Sprint 1"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("description => Optional.empty()")
    void testDescription() {
      Qual q = simpleQual("description", Operator.EQUALS, stringValue("some text"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("self => Optional.empty()")
    void testSelf() {
      Qual q = simpleQual("self", Operator.EQUALS, stringValue("http://whatever"));
      Optional<String> res = jqlBuilder.mkJql(List.of(q)).get(0);
      assertTrue(res.isEmpty());
    }
  }

  // --------------------------------------------------------------------------
  // Combined usage example
  // --------------------------------------------------------------------------
  @Test
  @DisplayName("Combined usage => multiple columns => buildJqlQuery joined by AND")
  void testCombinedBuild() {
    // 1) key < "ABC-123" => issueKey < "ABC-123"
    Qual issueKeyLt = simpleQual("key", Operator.LESS_THAN, stringValue("ABC-123"));
    // 2) project_name != null => project IS NOT NULL
    Qual projectNeNull = simpleQual("project_name", Operator.NOT_EQUALS, nullValue());
    // 3) status = 'Open' => status = "Open"
    Qual statusEq = simpleQual("status", Operator.EQUALS, stringValue("Open"));
    // 4) labels => unsupported => omitted

    String jql =
        jqlBuilder.buildJqlQuery(
            List.of(
                issueKeyLt,
                projectNeNull,
                statusEq,
                simpleQual("labels", Operator.EQUALS, stringValue("something"))));

    // Expect: "issueKey < \"ABC-123\" AND project IS NOT NULL AND status = \"Open\""
    // (labels is dropped)
    String expected = "issueKey < \"ABC-123\" AND project IS NOT NULL AND status = \"Open\"";
    assertEquals(expected, jql);
  }
}
