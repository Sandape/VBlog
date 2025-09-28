package org.sang.bean;

import java.util.List;

/**
 * 表元数据类
 * 表示AI解析SQL后得到的表结构信息
 *
 * @author sang
 * @date 2024
 */
public class TableMetaData {

    /**
     * 表名
     */
    private String name;

    /**
     * 字段列表
     */
    private List<ColumnMetaData> colum;

    /**
     * 实体路径
     */
    private String entityPath;

    /**
     * 原始SQL语句
     */
    private String originalSql;

    public TableMetaData() {
    }

    public TableMetaData(String name, List<ColumnMetaData> colum, String entityPath, String originalSql) {
        this.name = name;
        this.colum = colum;
        this.entityPath = entityPath;
        this.originalSql = originalSql;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ColumnMetaData> getColum() {
        return colum;
    }

    public void setColum(List<ColumnMetaData> colum) {
        this.colum = colum;
    }

    public String getEntityPath() {
        return entityPath;
    }

    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public void setOriginalSql(String originalSql) {
        this.originalSql = originalSql;
    }

    /**
     * 字段元数据类
     */
    public static class ColumnMetaData {
        /**
         * 字段名
         */
        private String columName;

        /**
         * 字段类型
         */
        private String columType;

        /**
         * 字段描述
         */
        private String columdesc;

        public ColumnMetaData() {
        }

        public ColumnMetaData(String columName, String columType, String columdesc) {
            this.columName = columName;
            this.columType = columType;
            this.columdesc = columdesc;
        }

        public String getColumName() {
            return columName;
        }

        public void setColumName(String columName) {
            this.columName = columName;
        }

        public String getColumType() {
            return columType;
        }

        public void setColumType(String columType) {
            this.columType = columType;
        }

        public String getColumdesc() {
            return columdesc;
        }

        public void setColumdesc(String columdesc) {
            this.columdesc = columdesc;
        }

        @Override
        public String toString() {
            return "ColumnMetaData{" +
                    "columName='" + columName + '\'' +
                    ", columType='" + columType + '\'' +
                    ", columdesc='" + columdesc + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "TableMetaData{" +
                "name='" + name + '\'' +
                ", colum=" + colum +
                ", entityPath='" + entityPath + '\'' +
                ", originalSql='" + originalSql + '\'' +
                '}';
    }
}
