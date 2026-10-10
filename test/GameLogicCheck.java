/**
 * Headless checks for the game rules. Nothing is drawn and no window is
 * opened, so this runs in CI: it only needs javafx.graphics on the classpath
 * because the game objects reference Color and GraphicsContext.
 *
 *   java GameLogicCheck      exit code is non-zero if any check fails
 */
public class GameLogicCheck {

    private static int failures = 0;

    public static void main(String[] args) {
        checkEnemyPointsAndHealth();
        checkTorpedoMovementAndContact();
        checkCoralBarrier();
        checkSubmarineHitsAndShield();
        checkPowerUpEffects();

        if (failures > 0) {
            System.out.println(failures + " check(s) FAILED");
            System.exit(1);
        }
        System.out.println("All checks passed");
    }

    private static void checkEnemyPointsAndHealth() {
        SeaCreature jellyfish = SeaCreature.createJellyfish(100, 100, 1);
        SeaCreature squid = SeaCreature.createSquid(100, 100, 1);
        SeaCreature anglerfish = SeaCreature.createAnglerfish(100, 100, 1);
        SeaCreature kraken = SeaCreature.createKraken(100, 100, 1, 5);

        expect(jellyfish.getPoints() == 10, "jellyfish is worth 10 points");
        expect(squid.getPoints() == 20, "squid is worth 20 points");
        expect(anglerfish.getPoints() == 30, "anglerfish is worth 30 points");
        expect(kraken.getPoints() == 100, "kraken is worth 100 points");

        jellyfish.takeDamage(1);
        expect(!jellyfish.isAlive(), "jellyfish dies after one hit");

        anglerfish.takeDamage(1);
        expect(anglerfish.isAlive(), "anglerfish survives the first hit");
        anglerfish.takeDamage(1);
        expect(!anglerfish.isAlive(), "anglerfish dies after two hits");

        // wave 5 boss: 18 + 5 * 3 = 33 hit points
        for (int hit = 0; hit < 32; hit++) {
            kraken.takeDamage(1);
        }
        expect(kraken.isAlive(), "wave-5 kraken survives 32 hits");
        kraken.takeDamage(1);
        expect(!kraken.isAlive(), "wave-5 kraken dies on hit 33");

        // the boss ignores the formation's edge-reversal and step-down
        SeaCreature boss = SeaCreature.createKraken(100, 100, 1, 5);
        double yBefore = boss.getPos()[1];
        boss.stepDown();
        expect(boss.getPos()[1] == yBefore, "kraken does not step down with the formation");

        SeaCreature grunt = SeaCreature.createSquid(100, 100, 1);
        grunt.stepDown();
        expect(grunt.getPos()[1] == 114, "formation enemies step down 14 px");
        System.out.println("enemies: points, hit points and boss movement rules hold");
    }

    private static void checkTorpedoMovementAndContact() {
        Torpedo player = new Torpedo(200, 300, true);
        Torpedo enemy = new Torpedo(200, 300, false);
        player.update();
        enemy.update();
        expect(player.getY() == 292, "player torpedo travels up 8 px per frame");
        expect(enemy.getY() == 304, "enemy shot travels down 4 px per frame");
        expect(player.isFriendly() && !enemy.isFriendly(), "torpedoes know who fired them");

        Torpedo probe = new Torpedo(100, 100, true);
        expect(probe.inContact(110, 100, 16), "torpedo within radius counts as a hit");
        expect(!probe.inContact(200, 100, 16), "distant torpedo is not a hit");
        System.out.println("torpedoes: speed, direction and contact test hold");
    }

    private static void checkCoralBarrier() {
        Coral coral = new Coral(100, 400);
        expect(coral.containsPoint(120, 420), "point inside the coral is blocked");
        expect(!coral.containsPoint(300, 420), "point beside the coral is not blocked");
        for (int hit = 0; hit < 11; hit++) {
            coral.takeDamage(1);
        }
        expect(!coral.isDestroyed(), "coral survives 11 hits");
        expect(coral.takeDamage(1), "the 12th hit destroys the coral");
        expect(coral.isDestroyed(), "destroyed coral reports destroyed");
        System.out.println("coral: blocks shots and breaks after 12 hits");
    }

    private static void checkSubmarineHitsAndShield() {
        Submarine sub = new Submarine(350, 550);
        expect(sub.hit(), "an unshielded hit costs a life");
        expect(!sub.hit(), "a second hit inside the invincibility window is ignored");
        for (int frame = 0; frame < 61; frame++) {
            sub.update();
        }
        expect(sub.hit(), "after the window closes, a hit costs a life again");

        Submarine shielded = new Submarine(350, 550);
        shielded.activateShield();
        expect(!shielded.hit(), "the shield absorbs one hit");
        for (int frame = 0; frame < 61; frame++) {
            shielded.update();
        }
        expect(shielded.hit(), "the shield only absorbs one hit");
        System.out.println("submarine: lives, invincibility window and shield hold");
    }

    private static void checkPowerUpEffects() {
        Submarine sub = new Submarine(350, 550);

        PowerUp spread = new SpreadShot(350, 550);
        expect(!sub.isSpreadShot(), "spread shot starts off");
        spread.activate(sub);
        expect(sub.isSpreadShot(), "spread shot power-up switches spread shot on");
        for (int frame = 0; frame < 601; frame++) {
            sub.update();
        }
        expect(!sub.isSpreadShot(), "spread shot expires after 600 frames");

        PowerUp charge = new DepthCharge(350, 550);
        charge.activate(sub);
        expect(sub.isDepthChargePending(), "depth charge arms the submarine");
        sub.consumeDepthCharge();
        expect(!sub.isDepthChargePending(), "the game loop clears the depth charge");

        PowerUp shield = new Shield(350, 550);
        shield.activate(sub);
        expect(!sub.hit(), "shield power-up absorbs the next hit");

        PowerUp falling = new Shield(100, 100);
        falling.update();
        expect(falling.getY() == 102, "power-ups fall 2 px per frame");
        System.out.println("power-ups: each one applies its own effect through activate()");
    }

    private static void expect(boolean condition, String what) {
        if (!condition) {
            failures++;
            System.out.println("FAILED: " + what);
        }
    }
}
