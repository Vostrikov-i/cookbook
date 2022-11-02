package other.test.examples;

public class OutputResultCsv {

    private String field1;
    private String field2;

    public OutputResultCsv(String field1, String field2) {
        this.field1 = field1;
        this.field2 = field2;
    }

    public String getField1() {
        return field1;
    }

    public String getField2() {
        return field2;
    }

    @Override
    public String toString() {
        return "{" +
                "field1='" + field1 + '\'' +
                ", field2='" + field2 + '\'' +
                '}';
    }
}
