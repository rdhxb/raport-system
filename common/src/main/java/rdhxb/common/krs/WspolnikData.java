package rdhxb.common.krs;

public record WspolnikData(
        String nazwa,
        String nip,
        String krs,
        String udzialy,
        Boolean caloscUdzialow
) {}
