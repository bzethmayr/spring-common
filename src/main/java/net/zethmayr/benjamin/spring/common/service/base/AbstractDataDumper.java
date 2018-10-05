package net.zethmayr.benjamin.spring.common.service.base;

import net.zethmayr.benjamin.spring.common.mapper.base.Mapper;
import net.zethmayr.benjamin.spring.common.repository.base.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.MULTILINE;

/**
 * Base class for CSV-generating services. Allows more-or-less declarative construction
 * of CSV reports on the data, including basic aggregation.
 */
public abstract class AbstractDataDumper<C> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataDumper.class);

    /**
     * The canonical String representation of {@code null}
     */
    protected static final String NULL = "" + null;

    /**
     * A quote.
     */
    protected static final String QUOTE = "\"";

    /**
     * Two quotes.
     */
    protected static final String TWOQUOTES = QUOTE + QUOTE;

    /**
     * Canonicalizes null or empty strings or "null" to empty strings, else identity.
     */
    protected static final Function<String, String> NULL_IS_NULL_IS_NULL_IS_EMPTY =
            (value) -> value == null ? "" : NULL.equals(value) ? "" : value;

    /**
     * Performs Excel-style CSV quoting and quote escaping, after null canonicalization.
     */
    protected static final Function<String, String> QUOTED = NULL_IS_NULL_IS_NULL_IS_EMPTY.andThen(
            (value) -> QUOTE + value.replace(QUOTE, TWOQUOTES) + QUOTE);

    private static final Pattern MUST_QUOTE = Pattern.compile("[\",\r\n]", MULTILINE);
    /**
     * Conditionally quotes fields, after null canonicalization.
     * Fields are quoted if they contain commas, quotes, cr, or lf.
     */
    protected static final Function<String, String> QUOTE_IF_NEEDED = NULL_IS_NULL_IS_NULL_IS_EMPTY.andThen(
            (value) -> {
                final Matcher m = MUST_QUOTE.matcher(value);
                if (m.find()) {
                    return QUOTED.apply(value);
                }
                return value;
            });

    protected enum SqlOp {
        LT("<"),
        LTE("<="),
        GT(">"),
        GTE(">="),
        EQ("="),
        LIKE("LIKE");

        public final String sql;

        SqlOp(final String sql) {
            this.sql = sql;
        }
    }

    /**
     * Adds WHERE clauses to the source query for a dump.
     *
     * @param <C> The instance type
     * @param <I> The instance field type
     * @param <O> The JDBC field type
     */
    protected static class DumpFilter<C, I, O> {
        private final Mapper<C, I, O> field;
        private final String sqlOp;
        private final I value;

        /**
         * Creates a filter for the given field, operation, and value.
         *
         * @param field The field mapper
         * @param sqlOp The operation, e.g. "{@code <}"
         * @param value The r-value for the operation
         */
        public DumpFilter(final Mapper<C, I, O> field, final SqlOp sqlOp, final I value) {
            this.field = field;
            this.sqlOp = sqlOp.sql;
            this.value = value;
        }

        /**
         * Returns a parameterized WHERE clause condition based on the field and operation.
         *
         * @param first Whether this is the first field
         * @return A SQL condition
         */
        public String sql(final boolean first) {
            return (first ? " WHERE " : " AND ")
                    + field.fieldName
                    + " " + sqlOp + " ?";
        }

        /**
         * Returns the r-value set at construction.
         *
         * @return The JDBC serialization of the r-value
         */
        public O sqlValue() {
            return field.ser(value);
        }
    }

    /**
     * Convenience method for constructing filters.
     *
     * @param fieldMapper The field mapper
     * @param sqlOp       The operation, e.g. "{@code <}"
     * @param value       The r-value for the operation
     * @param <C>         The instance type
     * @param <I>         The instance field type
     * @param <O>         The JDBC field type
     * @return A filter
     */
    protected static <C, I, O> DumpFilter<C, I, O> filter(final Mapper<C, I, O> fieldMapper, final SqlOp sqlOp, final I value) {
        return new DumpFilter<>(fieldMapper, sqlOp, value);
    }

    /**
     * Creates a CSV column based on a function of a result datum, usually a getter.
     *
     * @param <C> The instance type
     */
    protected static class DumpExtractor<C> {
        private final String headerName;
        private final Function<C, String> valueExtractor;
        private final Function<String, String> valueFormatter;

        private Function<String, String> aggregator = (s) -> "";

        DumpExtractor(
                final String headerName,
                final Function<C, String> valueExtractor,
                final Function<String, String> valueFormatter
        ) {
            this.headerName = headerName;
            this.valueExtractor = valueExtractor;
            this.valueFormatter = valueFormatter;
        }

        public String headerName() {
            return headerName;
        }

        public String extractValue(final C row) {
            final String value = valueExtractor.apply(row);
            aggregator.apply(value);
            return valueFormatter.apply(value);
        }

        public DumpExtractor<C> aggregator(final Function<String, String> aggregator) {
            if (aggregator != null) {
                this.aggregator = aggregator;
            }
            return this;
        }

        public String aggregate() {
            return aggregator.apply(null);
        }
    }

    /**
     * A summing aggregator for decimal values.
     *
     * @return An aggregator
     */
    protected Function<String, String> summingAggregator() {
        final AtomicReference<String> enclosed = new AtomicReference<>(null);
        return (s) -> enclosed.getAndAccumulate(s, (p, n) -> {
            if (n == null) {
                return ""; // self-destructs on readout...
            }
            if ("".equals(n) || NULL.equals(n)) {
                return p;
            }
            if (p == null || "".equals(p) || NULL.equals(p)) {
                return n;
            }
            return new BigDecimal(p).add(new BigDecimal(n)).toPlainString();
        });
    }

    /**
     * Subclasses provide CSV column definitions via this method.
     *
     * @return The column definitions
     */
    protected abstract List<DumpExtractor<C>> constructExtractors();

    /**
     * Subclasses control whether aggregates are printed via this method.
     *
     * @return Whether aggregate rows are printed
     */
    protected abstract boolean aggregate();

    /**
     * Dumps CSV data to a file.
     *
     * @param outFile    The output file
     * @param repository The repository to take data from
     * @param filters    Any filters on the query
     */
    @SafeVarargs
    protected final void dump(final File outFile, final Repository<C, ?> repository, final DumpFilter<C, ?, ?>... filters) {
        PrintStream out = null; // Strongly considering making this the parameter...
        boolean mustClose = true;
        try {
            if (outFile == null) {
                out = System.out;
                mustClose = false;
            } else {
                out = new PrintStream(new FileOutputStream(outFile));
            }
            dump(out, repository, filters);
        } catch (Exception e) {
            throw ServiceException.because(e);
        } finally {
            if (mustClose && out != null) {
                out.close();
            }
        }
    }

    @SafeVarargs
    protected final void dump(final PrintStream out, final Repository<C, ?> repository, final DumpFilter<C, ?, ?>... filters) {
        List<DumpExtractor<C>> extractors = constructExtractors(); // preconstruct?
        printHeaders(out, extractors);
        final StringBuilder sql = new StringBuilder(repository.select());
        final Object[] values = new Object[filters.length];
        boolean first = true;
        for (int i = 0; i < filters.length; i++) {
            sql.append(filters[i].sql(first));
            values[i] = filters[i].sqlValue();
            first = false;
        }
        LOG.debug("sql is {}", sql);
        final List<C> items = repository.getUnsafe(sql.toString(), values);
        for (final C item : items) {
            printItem(out, item, extractors);
        }
        if (aggregate()) {
            printAggregates(out, extractors);
        }
    }

    /**
     * Creates an extractor for a string field,
     * with a transformation applied.
     *
     * @param concreteMapper   The field mapper
     * @param valueTransformer Any transform function for output
     * @param <C>              The instance type
     * @return An extractor for the string field value
     */
    protected static <C> DumpExtractor<C> stringExtractor(
            final Mapper<C, String, String> concreteMapper,
            final Function<String, String> valueTransformer) {
        return new DumpExtractor<>(concreteMapper.fieldName, concreteMapper::serFrom, valueTransformer);
    }

    /**
     * Creates an extractor for a string field.
     *
     * @param concreteMapper The field mapper
     * @param <C>            The instance type
     * @return An extractor for the string field value
     */
    protected static <C> DumpExtractor<C> stringExtractor(
            final Mapper<C, String, String> concreteMapper
    ) {
        return stringExtractor(concreteMapper, NULL_IS_NULL_IS_NULL_IS_EMPTY);
    }

    /**
     * Creates an extractor for a field with a readable {@link #toString}.
     *
     * @param headerName     The header name for the column
     * @param concreteMapper The field mapper
     * @param <C>            The instance type
     * @param <I>            The instance field type
     * @param <O>            The JDBC field type
     * @return An extractor for the field's string value
     */
    protected static <C, I, O> DumpExtractor<C> stringableExtractor(
            final String headerName,
            final Mapper<C, I, O> concreteMapper
    ) {
        return new DumpExtractor<>(
                headerName,
                (c) -> "" + concreteMapper.serFrom(c),
                NULL_IS_NULL_IS_NULL_IS_EMPTY
        );
    }

    /**
     * Creates an extractor for a field with a readable {@link #toString},
     * using the field name as the header name.
     *
     * @param concreteMapper The field mapper
     * @param <C>            The instance type
     * @param <I>            The instance field type
     * @param <O>            The JDBC field type
     * @return An extractor for the field's string value
     */
    protected static <C, I, O> DumpExtractor<C> stringableExtractor(
            final Mapper<C, I, O> concreteMapper
    ) {
        return stringableExtractor(concreteMapper.fieldName(), concreteMapper);
    }

    /**
     * Creates an extractor indicating whether a set field contains a given enum value.
     *
     * @param headerName    The header name for the column
     * @param getCollection The set getter
     * @param setValue      The value sought
     * @param <C>           The instance type
     * @param <T>           The value
     * @param <S>           The set type
     * @return An extractor producing "Yes" or ""
     */
    protected static <C, T extends Enum<T>, S extends Set<T>> DumpExtractor<C> setHasValueExtractor(
            final String headerName,
            final Function<C, S> getCollection,
            final T setValue
    ) {
        return new DumpExtractor<>(
                headerName,
                (c) -> getCollection.apply(c).contains(setValue) ? "Yes" : "",
                (s) -> s
        );
    }

    /**
     * Creates an extractor for a collection field item meeting a given condition.
     *
     * @param headerName     The header name for the column
     * @param getList        The collection getter
     * @param itemFilter     The item condition
     * @param valueExtractor The item string formatter
     * @param <C>            The instance type
     * @param <X>            The item type
     * @param <L>            The instance field type
     * @return An extractor producing the first matching item or ""
     */
    protected static <C, X, L extends Collection<X>> DumpExtractor<C> listFilteredValueExtractor(
            final String headerName,
            final Function<C, L> getList,
            final Predicate<X> itemFilter,
            final Function<X, String> valueExtractor
    ) {
        return new DumpExtractor<>(
                headerName,
                (c) -> getList.apply(c).stream()
                        .filter(Objects::nonNull)
                        .filter(itemFilter)
                        .findFirst()
                        .map(valueExtractor)
                        .orElse(""),
                NULL_IS_NULL_IS_NULL_IS_EMPTY
        );
    }

    private void printItem(final PrintStream out, final C item, final List<DumpExtractor<C>> extractors) {
        dumpLine(out, extractors.stream()
                .map((extractor) -> extractor.extractValue(item))
                .collect(Collectors.joining(","))
        );
    }

    private void printHeaders(final PrintStream out, final List<DumpExtractor<C>> extractors) {
        dumpLine(out, extractors.stream()
                .map(DumpExtractor::headerName)
                .map(QUOTED)
                .collect(Collectors.joining(","))
        );
    }

    private void printAggregates(final PrintStream out, final List<DumpExtractor<C>> extractors) {
        dumpLine(out, extractors.stream()
                .map((d) -> d.valueFormatter.apply(d.aggregate()))
                .collect(Collectors.joining(","))
        );
    }

    private static void dumpLine(final PrintStream out, final String line) {
        out.println(line);
        LOG.trace(line);
    }
}
