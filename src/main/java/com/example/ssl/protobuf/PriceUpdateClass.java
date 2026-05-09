package com.example.ssl.protobuf;

public final class PriceUpdateClass {

    public static final class PriceUpdate {
        private String symbol;
        private String price;

        private PriceUpdate(Builder builder) {
            this.symbol = builder.symbol;
            this.price = builder.price;
        }

        public String getSymbol() { return symbol; }
        public String getPrice() { return price; }

        public byte[] toByteArray() {
            byte[] symbolBytes = symbol.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] priceBytes = price.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            try {
                // field 1 (symbol): tag = (1 << 3) | 2 = 0x0a
                out.write(0x0a);
                writeVarint(out, symbolBytes.length);
                out.write(symbolBytes);

                // field 2 (price): tag = (2 << 3) | 2 = 0x12
                out.write(0x12);
                writeVarint(out, priceBytes.length);
                out.write(priceBytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return out.toByteArray();
        }

        private void writeVarint(java.io.ByteArrayOutputStream out, int value) {
            while ((value & ~0x7F) != 0) {
                out.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
            out.write(value);
        }

        public static Builder newBuilder() { return new Builder(); }

        public static final class Builder {
            private String symbol = "";
            private String price = "";

            public Builder setSymbol(String symbol) { this.symbol = symbol; return this; }
            public Builder setPrice(String price) { this.price = price; return this; }
            public PriceUpdate build() { return new PriceUpdate(this); }
        }
    }

    private PriceUpdateClass() {}
}