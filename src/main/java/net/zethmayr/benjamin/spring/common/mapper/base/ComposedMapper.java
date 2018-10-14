package net.zethmayr.benjamin.spring.common.mapper.base;

import net.zethmayr.benjamin.spring.common.util.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.zethmayr.benjamin.spring.common.Constants.MONEY_CONTEXT;

/**
 * This is the primary concrete field mapper implementation.
 * {@inheritDoc}
 */
/*
 * This implementation composes arbitrary implementations of field mapping components.
 */
public class ComposedMapper<C, I, O> extends Mapper<C, I, O> {
    private static final Logger LOG = LoggerFactory.getLogger(ComposedMapper.class);

    private final Function<C, I> cGetter;
    private final SerMapper<I, O> serMapper;
    private final RsGetter<O> rsGetter;
    private final DesMapper<I, O> desMapper;
    private final BiConsumer<C, I> cSetter;
    private final String sqlType;
    private final ColumnType<O> columnType;
    private final Class<O> externalClass;

    private int ordinal = -1;
    private PsSetter<O> psSetter;

    /**
     * Internal constructor. See {@link #field(String, Function, SerMapper, ColumnType, DesMapper, BiConsumer)}.
     *
     * @param fieldName  The SQL name of the field
     * @param cGetter    The accessor to retrieve the field value from an instance of the class
     * @param serMapper  The mapper to serialize a field value to a JDBC value
     * @param columnType The SQL column type information
     * @param desMapper  The mapper to deserialize a JDBC value to a field value
     * @param cSetter    The accessor to set the field value into an instance of the class
     */
    ComposedMapper(final String fieldName, final Function<C, I> cGetter, final SerMapper<I, O> serMapper, final ColumnType<O> columnType, final DesMapper<I, O> desMapper, final BiConsumer<C, I> cSetter) {
        super(fieldName);
        this.cGetter = cGetter;
        this.serMapper = serMapper;
        this.rsGetter = columnType.getterFactory().field(fieldName);
        this.desMapper = desMapper;
        this.cSetter = cSetter;
        sqlType = columnType.sqlType();
        externalClass = columnType.getExternalClass();
        this.columnType = columnType;
        LOG.trace("Created column mapper {}", this);
    }

    @Override
    void setInsertOrdinal(final int ordinal) {
        if (this.ordinal >= 0 && ordinal != this.ordinal) {
            throw MappingException.badSetup("Conflicting ordinal set");
        }
        this.ordinal = ordinal;
        psSetter = columnType.setterFactory().getForInsert(ordinal);
    }

    @Override
    public int getInsertOrdinal() {
        return ordinal;
    }

    @Override
    public void apply(final PreparedStatement insertPs, final O value) {
        if (this.ordinal < 0) {
            throw MappingException.badSetup("Ordinal not set");
        }
        psSetter.apply(insertPs, value);
    }

    @Override
    public ColumnType<O> getColumnType() {
        return columnType;
    }

    @Override
    public String sqlType() {
        return sqlType;
    }

    @Override
    public Class<O> getExternalClass() {
        return externalClass;
    }

    @Override
    public I getFrom(C container) {
        return cGetter.apply(container);
    }

    @Override
    public I des(O ser) throws MappingException {
        return ser == null ? null : desMapper.des(ser);
    }

    @Override
    public O from(ResultSet rs) throws MappingException {
        return rsGetter.from(rs);
    }

    @Override
    public O ser(I des) throws MappingException {
        return des == null ? null : columnType.limited(serMapper.ser(des));
    }

    @Override
    public void setTo(C container, I value) {
        cSetter.accept(container, value);
    }

    /**
     * Creates a mapper
     * for an arbitrarily-specified field.
     *
     * @param fieldName  The field name
     * @param cGetter    The instance getter method
     * @param ser        The serializer method
     * @param columnType The SQL type information
     * @param des        The deserializer method
     * @param cSetter    The instance setter method
     * @param <C>        The instance type
     * @param <I>        The instance field type
     * @param <O>        The JDBC field type
     * @return A field mapper
     */
    public static <C, I, O> Mapper<C, I, O> field(final String fieldName, final Function<C, I> cGetter, final SerMapper<I, O> ser, final ColumnType<O> columnType, DesMapper<I, O> des, final BiConsumer<C, I> cSetter) {
        return new ComposedMapper<>(
                fieldName,
                cGetter,
                ser,
                columnType,
                des,
                cSetter
        );
    }

    /**
     * Creates a mapper
     * for a field where the instance and JDBC types are the same.
     *
     * @param fieldName  The field name
     * @param cGetter    The instance getter method
     * @param columnType The SQL type information
     * @param cSetter    The instance setter method
     * @param <C>        The instance type
     * @param <O>        The field type
     * @return A field mapper
     */
    public static <C, O> Mapper<C, O, O> simpleField(final String fieldName, final Function<C, O> cGetter, final ColumnType<O> columnType, final BiConsumer<C, O> cSetter) {
        return new ComposedMapper<>(
                fieldName,
                cGetter,
                (in) -> in,
                columnType,
                (out) -> out,
                cSetter
        );
    }

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    /**
     * Creates a mapper
     * for a field representing money values with 2-digit precision.
     *
     * @param fieldName The field name
     * @param cGetter   The instance getter method
     * @param cSetter   The instance setter method
     * @param <C>       The instance type
     * @return A field mapper
     */
    public static <C> Mapper<C, BigDecimal, Long> money(final String fieldName, final Function<C, BigDecimal> cGetter, final BiConsumer<C, BigDecimal> cSetter) {
        return new ComposedMapper<>(
                fieldName,
                cGetter,
                (in) -> in.multiply(ONE_HUNDRED).longValue(),
                ColumnType.LONG,
                (out) -> Functions.money(new BigDecimal(out)).divide(ONE_HUNDRED, MONEY_CONTEXT.getRoundingMode()),
                cSetter
        );
    }

    /**
     * Creates a mapper
     * for a field which records the id for a table representing enums.
     *
     * @param ordinal     The serializer method (see {@link Enum#ordinal()}
     * @param fromOrdinal The deserializer method
     * @param <C>         The instance type
     * @return A field mapper
     */
    public static <C extends Enum<C>> Mapper<C, C, Integer> enumId(final SerMapper<C, Integer> ordinal, final DesMapper<C, Integer> fromOrdinal) {
        return new ComposedMapper<>(
                "id",
                Function.identity(),
                ordinal,
                ColumnType.INTEGER,
                fromOrdinal,
                cantSetTo()
        );
    }

    /**
     * Creates a mapper
     * for a field which records a field of an enum.
     *
     * @param fieldName  The field name
     * @param cGetter    The instance getter method
     * @param ser        The serializer method
     * @param columnType The SQL type information
     * @param des        The deserializer method
     * @param <C>        The instance type
     * @param <I>        The instance field type
     * @param <O>        The JDBC field type
     * @return A field mapper
     */
    public static <C extends Enum<C>, I, O> Mapper<C, I, O> enumField(final String fieldName, final Function<C, I> cGetter, final SerMapper<I, O> ser, final ColumnType<O> columnType, DesMapper<I, O> des) {
        return new ComposedMapper<>(
                fieldName,
                cGetter,
                ser,
                columnType,
                des,
                cantSetTo()
        );
    }

    /**
     * Creates a mapper
     * for a field where the instance and JDBC types are the same
     * which records a field of an enum.
     *
     * @param fieldName  The field name
     * @param cGetter    The instance getter method
     * @param columnType The SQL type information
     * @param <C>        The instance type
     * @param <O>        The field type
     * @return A field mapper
     */
    public static <C extends Enum<C>, O> Mapper<C, O, O> enumSimple(final String fieldName, final Function<C, O> cGetter, final ColumnType<O> columnType) {
        return new ComposedMapper<>(
                fieldName,
                cGetter,
                (in) -> in,
                columnType,
                (out) -> out,
                cantSetTo()
        );
    }
}

