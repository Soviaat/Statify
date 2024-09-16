package dev.soviaat;

import net.fabricmc.api.ClientModInitializer;

public class StatifyClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        // LOGGER.info("Statify Client is being initialized...");
    }
}

//    private void writeStatsToFile(ClientPlayerEntity player){
//        StatHandler statHandler = player.getStatHandler();
//        StringBuilder statsBuilder = new StringBuilder();
//
//        Set<Map.Entry<RegistryKey<Identifier>, Identifier>> stats = Stats.CUSTOM.getRegistry().getEntrySet();
//
//        for (Map.Entry<RegistryKey<Identifier>, Identifier> entry : stats) {
//            Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(entry.getValue());
//            int value = statHandler.getStat(stat);
//            String statName = entry.getValue().toString();
//
//            LOGGER.info("\tStat: {} = {}", statName, value);
//            statsBuilder.append(statName).append(": ").append(value).append("\n");
//        }
//
//        File file = new File("Statify/statify_stats.txt");
//        file.getParentFile().mkdirs();
//
//        try (FileWriter writer = new FileWriter(file)) {
//            writer.write("Statistics for " + player.getName().getString() + "\n at tick " + lastUpdateTime + "\n");
//            writer.write(statsBuilder.toString());
//        } catch (IOException e) {
//            LOGGER.error("Failed to write stats to file", e);
//        }
//    }
//}
