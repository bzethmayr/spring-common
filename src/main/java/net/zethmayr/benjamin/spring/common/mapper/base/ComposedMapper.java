package net.zethmayr.benjamin.spring.common.mapper.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This is the primary concrete field mapper implementation.
 * @param <C> The class whose field this maps
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
     * @param fieldName The SQL name of the field
     * @param cGetter The accessor to retrieve the field value from an instance of the class
     * @param serMapper The mapper to serialize a field value to a JDBC value
     * @param columnType The SQL column type
     * @param desMapper The mapper to deserialize a JDBC value to a field value
     * @param cSetter The accessor to set the field value into an instance of the class
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
        LOG.info("Created column mapper {}", this);
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

    public static <C, O> Mapper<C, O, O> direct(final String fieldName, final Function<C, O> cGetter, final ColumnType<O> columnType, final BiConsumer<C, O> cSetter) {
        return new ComposedMapper<>(
                fieldName,
                cGetter,
                (in) -> in,
                columnType,
                (out) -> out,
                cSetter
        );
    }

    public static <C> Mapper<C, C, Integer> enumId(final SerMapper<C, Integer> ordinal, final DesMapper<C, Integer> fromOrdinal) {
        return new ComposedMapper<>(
                "id",
                Function.identity(),
                ordinal,
                ColumnType.INTEGER,
                fromOrdinal,
                cantSetTo()
        );
    }

    public static <C, I, O> Mapper<C, I, O> enumField(final String fieldName, final Function<C, I> cGetter, final SerMapper<I, O> ser, final ColumnType<O> columnType, DesMapper<I, O> des) {
        return new ComposedMapper<>(
                fieldName,
                cGetter,
                ser,
                columnType,
                des,
                cantSetTo()
        );
    }

    public static <C, O> Mapper<C, O, O> enumDirect(final String fieldName, final Function<C, O> cGetter, final ColumnType<O> columnType) {
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

