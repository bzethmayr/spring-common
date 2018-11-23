# spring-common

Things helpful for simple Spring Boot JDBC web applications,
including:
 * a trivial ORM implementation
   * featuring insertion, deletion and materialization of arbitrary POJOs or enums
     * across basic JOINs
   * not featuring any UPDATE capability
 * declarative creation of CSV reports,
 * a hierarchical command parser,
 * and some other stuff

Note that this is targeted rather specifically at small applications vs large, and therefore uses approaches in places which would not be appropriate for larger applications.

