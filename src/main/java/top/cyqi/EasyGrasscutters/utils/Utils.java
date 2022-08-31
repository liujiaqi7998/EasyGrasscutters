package top.cyqi.EasyGrasscutters.utils;


import top.cyqi.EasyGrasscutters.EasyGrasscutters;

public class Utils {

    public static String GetDispatchAddress() {

        return "ws" + (EasyGrasscutters.getServerConfig().server.http.encryption.useEncryption ? "s" : "") + "://" +
                (EasyGrasscutters.getServerConfig().server.http.accessAddress.isEmpty() ? EasyGrasscutters.getServerConfig().server.http.bindAddress : EasyGrasscutters.getServerConfig().server.http.accessAddress) +
                ":" + (EasyGrasscutters.getServerConfig().server.http.accessPort != 0 ? EasyGrasscutters.getServerConfig().server.http.accessPort : EasyGrasscutters.getServerConfig().server.http.bindPort);
    }

    public static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }

    public static String generateRandomNumber(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
