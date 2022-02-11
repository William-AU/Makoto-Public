package bot.storage.images;

import java.util.*;

public class MakotoImages {
    private static final List<String> badImages = new ArrayList<>(){{
        add("https://img3.gelbooru.com//images/8d/ce/8dceedaaee4582852f1f83049d61bbfa.jpg");
        add("https://img3.gelbooru.com//images/f2/8c/f28cb494f0beca305c9101b76a1e465f.jpg");
        add("https://img3.gelbooru.com//samples/b7/be/sample_b7be84f267a2635837967d5ddca0da6d.jpg");
        add("https://i.redd.it/cvzqmicci5p51.png");
        add("https://img3.gelbooru.com//images/cd/5c/cd5c2ec311036c25e5bacf698832a6c6.jpg");
    }};

    private static final List<String> decentImages = new ArrayList<>(){{
        add("https://img3.gelbooru.com/images/c1/8a/c18a1c6e1b5d1f9408a46cff2de0e434.jpeg");
        add("https://img3.gelbooru.com/images/af/2f/af2fca21fbce126641f13ba8699d3c7c.jpeg");
        add("https://img3.gelbooru.com/images/5f/0e/5f0e17472418df07fa3f77f4290238dd.png");
        add("https://img3.gelbooru.com/images/12/41/1241198db5b3078c90e235d6e6133f00.jpeg");
        add("https://img3.gelbooru.com//images/95/86/958677c3bc1a9c381fa1caee6d481654.jpeg");
        add("https://img3.gelbooru.com/images/ca/89/ca89f1d0bba4507f56455c4a9853e244.jpeg");
        add("https://img3.gelbooru.com//images/8c/00/8c0001d9a11c7f4ca0bc50596e231652.jpg");
        add("https://img3.gelbooru.com/images/51/98/519815d14c96b2a98d312c9a891d8292.jpeg");
        add("https://img3.gelbooru.com//images/e2/13/e2131a9a57ab1bd23450c1e0624750f7.png");
        add("https://img3.gelbooru.com/images/f6/78/f6784329a7f34a72e2234c321f27bac2.jpg");
        add("https://img3.gelbooru.com/images/00/3a/003a44eb79c61b76e6873d2b04aa605e.png");
        add("https://img3.gelbooru.com//images/da/41/da4139d1951a599b7a77625c8430e836.png");
        add("https://img3.gelbooru.com//images/f8/d9/f8d96de2208fe42d78dd56e40b46d195.jpg");
        add("https://img3.gelbooru.com//images/dd/34/dd3470b58b229eef574b42211fc64a1b.png");
        add("https://img3.gelbooru.com/images/4e/d4/4ed43c4e4196ea29708253d6c8b07798.jpg");
        add("https://img3.gelbooru.com//images/e1/16/e11691100465951f954116b751eac496.jpg");
        add("https://img3.gelbooru.com//images/7e/05/7e05efdce97d9e9ebb9a72de62c6d909.jpg");
        add("https://img3.gelbooru.com//images/0a/40/0a407402774be92dc432525d2879e94f.jpg");
        add("https://img3.gelbooru.com/images/77/02/7702440cefac9e34ce7141195ac289b1.png");
    }};

    private static final List<String> goodImages = new ArrayList<>(){{
        add("https://img3.gelbooru.com/images/55/d6/55d68063e37d7d74857b88a58b43ef4e.jpg");
        add("https://img3.gelbooru.com/images/42/c7/42c7686a1a512fc177310c3cada1b8c5.jpeg");
        add("https://img3.gelbooru.com/images/3e/ad/3eade5ecfd55974e4bb450312bae37b2.jpg");
        add("https://img3.gelbooru.com//samples/81/2f/sample_812fa398ffff07ee4933d76d3b759323.jpg");
        add("https://img3.gelbooru.com/images/a2/9f/a29f5daa6d1dbbb089af38c40f1cfd49.png");
        add("https://img3.gelbooru.com/images/2a/a1/2aa1a134545f411f15d9eaea623e6861.jpg");
        add("https://img3.gelbooru.com//images/48/fd/48fdd531b6cf79cb600ba7ecad8ce402.png");
        add("https://img3.gelbooru.com/images/be/c7/bec762ddb217e654d162fd28d08357ec.jpg");
    }};

    private static final List<String> veryBadImages = new ArrayList<>() {{
        add("https://img3.gelbooru.com//images/f1/d8/f1d801985ae9c748cb219f1531f8806b.png");
        add("https://img3.gelbooru.com//images/64/8a/648afb2ed14047f8ae2c89f377603a77.jpg");
        add("https://img3.gelbooru.com/images/17/6a/176acaf173ac19a27a7c02d84120f33c.png");
        add("https://img3.gelbooru.com//images/5d/cb/5dcbbc100c63af9d1f4742e99bbd62b0.jpg");
        add("https://img3.gelbooru.com/images/55/0f/550fb6bf6f4f86173dcb0b40d8567b4c.jpg");
        add("https://img3.gelbooru.com/images/b1/01/b10140b1c22df685534c5474ccacdb0e.jpg");
        add("https://img3.gelbooru.com/images/8a/97/8a97686cd24342baaca2456bb60ee259.png");
        add("https://img3.gelbooru.com//images/b4/c2/b4c2a873eba25627c321c9276f6faf78.png");
        add("https://img3.gelbooru.com/images/f4/f4/f4f45490e1cebe83d394f8b146490ca0.jpeg");
        add("https://img3.gelbooru.com/images/4c/8d/4c8dd8b1bc0d222d5b7032685adc7f58.jpeg");
        add("https://img3.gelbooru.com//images/58/7d/587d125cf656859d8d684311a7ce4726.jpeg");
        add("https://img3.gelbooru.com/images/2d/18/2d1852fd8017321c6c9749da11e719dd.jpg");
        add("https://img3.gelbooru.com//images/5b/1c/5b1cff2b9f0c3ac8cdf791accce11c48.png");
        add("https://img3.gelbooru.com/images/0e/c0/0ec0d904d4d00e6f487a5eeb55bc5bcb.png");
    }};

    public static String getBadImage(){
        Collections.shuffle(badImages);
        return badImages.get(0);
    };

    public static String getDecentImage(){
        Collections.shuffle(decentImages);
        return decentImages.get(0);
    };
    public static String getGoodImage(){
        Collections.shuffle(goodImages);
        return goodImages.get(0);
    };

    public static String getVeryBadImage() {
        Collections.shuffle(veryBadImages);
        return veryBadImages.get(0);
    }
}
