package com.gmail.fingrambbg.bettersleepremastered;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
// TODO: WORLD LIMITER, BASICALLY WHAT WORLDS DO PLAYERS SEE THIS
// TODO: MAYBE REMOVE NETHER AND END FROM CHOICE AS IT DOESNT MATTER
// TODO: ONLY OVERWORLD, ALSO ADD RAIN VOTE
// TODO: PRIZE

public class BetterSleepRemastered extends JavaPlugin implements Listener, Runnable {
	public static Plugin plugin;
	ArrayList<Player> yes;
	ArrayList<Player> no;
	ArrayList<Player> debug; 
	ArrayList<Player> prizes; 
	Phases phase;
	int countDown; 
	Player player;
	World mainWorld;
	BossBar bossBar; 
	long cooldown;
	int filler;
	Random rand = new Random();
	

	private enum Phases {
		CHECK, INIT, OPERATION, COMPLETE, CLEANUP, OFF
	}

	@Override
	public void onEnable() {
		mainWorld = Bukkit.getWorlds().get(0);
		cooldown = System.currentTimeMillis();
		plugin = this;
		phase = Phases.OFF;
		yes = new ArrayList<Player>();
		no = new ArrayList<Player>();
		debug = new ArrayList<Player>();
		prizes = new ArrayList<Player>();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("BetterSleep enabled, Config loaded");
	}
	
	@EventHandler
	public void onBed(PlayerBedEnterEvent event) {
		if(debug.contains(event.getPlayer())){
			event.getPlayer().sendMessage("Bed Entered: " + event.getPlayer().isSleeping());
		}
		prizes.add(event.getPlayer());
		sleepYes((CommandSender) event.getPlayer());
	}
	
	
	@EventHandler
	public void offBed(PlayerBedLeaveEvent event) {
		if(debug.contains(event.getPlayer())){
			
			event.getPlayer().sendMessage("Bed Left: " + !event.getPlayer().isSleeping());
		}
		prizes.remove(event.getPlayer());
		sleepNo((CommandSender) event.getPlayer());
	}
	
	@EventHandler
	public void joinGame(PlayerJoinEvent event){
		if(phase != Phases.OFF){
			bossBar.addPlayer(event.getPlayer());
		}
	}
	@EventHandler
	public void leaveGame(PlayerQuitEvent event){
		if(debug.contains(event.getPlayer())){
			debug.remove(event.getPlayer());
		}
		if(prizes.contains(event.getPlayer())){
			prizes.remove(event.getPlayer());
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("BetterSleep disabled");
		yes.clear();
		no.clear();
		debug.clear();
		prizes.clear();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("sleep")) {
				if(args.length < 1){ String[] temp = {"help"}; args = temp;}
				if (args[0].equalsIgnoreCase("yes")) {
					sleepYes(sender);
					return true;
				} else if (args[0].equalsIgnoreCase("no")) {
					sleepNo(sender);
					return true;
				} else if (args[0].equalsIgnoreCase("benefits")) {
					sleepBenefits(sender);
					return true;
				} else if(args[0].equalsIgnoreCase("reset")){
					cleanUp();
					return true;
				} else if(args[0].equalsIgnoreCase("debug")){
					debugToggle(sender);
					return true;
				}
				

				sleepHelp(sender);
			}
			return true;
		}
		sender.sendMessage("Console cannot use /sleep commands, sorry!");
		return true;
	}

	public void debugToggle(CommandSender sender){
		if(debug.contains(sender)){
			debug.remove((Player) sender);
			sender.sendMessage("Deactivated debug mode!");
		}
		else {
			debug.add((Player) sender);
			sender.sendMessage("Activated debug mode!");
		}
	}
	public void sleepYes(CommandSender sender) {
		if(debug.contains(sender)){
			sender.sendMessage("Yes Initiated");
		}
		if(!mainWorld.getPlayers().contains(sender)){
			sender.sendMessage(ChatColor.GOLD + "You are not in the right dimension");
			return;
		}
		else if (phase == Phases.OFF) {
			if(debug.contains(sender)){
				sender.sendMessage("Starting vote");
			}
			phase = Phases.CHECK;
			player = (Player) sender;
			run();
			return;
		} else if (yes.contains(sender)) {
			sender.sendMessage(ChatColor.GOLD + "You already voted yes!");
			return;
		} else if (no.contains(sender)) {
			no.remove(sender);
			if(debug.contains(sender)){
				sender.sendMessage("Removed from No arrayList " + !no.contains(sender));
			}
		}
		yes.add((Player) sender);
		if(debug.contains(sender)){
			sender.sendMessage("Added player to Yes arrayList " + yes.contains(sender));
		}
		sender.sendMessage(ChatColor.GOLD + "Voted yes!");
	}

	public void sleepNo(CommandSender sender) {
		if(!mainWorld.getPlayers().contains(sender)){
			sender.sendMessage(ChatColor.GOLD + "You are not in the right dimension");
			return;
		}
		else if (no.contains(sender)) {
			sender.sendMessage(ChatColor.GOLD + "You already voted no!");
			return;
		} else if (yes.contains(sender)) {
			yes.remove(sender);
			if(debug.contains(sender)){
				sender.sendMessage("Removed from Yes arrayList: " + !yes.contains(sender));
			}
		}
		no.add((Player) sender);
		if(debug.contains(sender)){
			sender.sendMessage("Added player to No arrayList: " + no.contains(sender));
		}
		sender.sendMessage(ChatColor.GOLD + "Voted no!");
	}

	public void sleepBenefits(CommandSender sender) {
		sender.sendMessage(ChatColor.BLUE + "--- Sleep Benefits ---");
		sender.sendMessage("Sleeping in a bed to vote gives unique chances!");
		sender.sendMessage("1/5 chance of getting Phantom Membrane");
		sender.sendMessage("1/5 chance of getting Prismarine Crystal");
		sender.sendMessage("1/5 chance of gaining Health");
		sender.sendMessage("More to come!");
	}

	public boolean sleepHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.BLUE + "--- Sleep Help ---");
		sender.sendMessage(ChatColor.GOLD + "/Sleep Yes" + ChatColor.WHITE + " - Votes yes or start vote");
		sender.sendMessage(ChatColor.GOLD + "/Sleep No" + ChatColor.WHITE + " - Votes no");
		sender.sendMessage(ChatColor.GOLD + "/Sleep Benefits" + ChatColor.WHITE + " - Reveals benefits to sleeping");
		sender.sendMessage(ChatColor.GOLD + "/Sleep Help" + ChatColor.WHITE + " - Displays this prompt");
		sender.sendMessage(ChatColor.GOLD + "/Sleep Debug" + ChatColor.WHITE + " - Toggles the debug mode!");
		sender.sendMessage(ChatColor.GOLD + "Broken? Activate debug mode and Contact RoboEx or Caliburn#01119 on Discord with details.");
		return true;
	}

	public void run() {
		if(debug.size() > 0){
			for(int i = 0; i < debug.size(); i++){
				debug.get(i).sendMessage("Switching phases to " + phase.name());
			}
		}
		// TODO Auto-generated method stub
		
		switch (phase) {
		case CHECK:
			check();
			break;
		case INIT:
			init();
			break;
		case OPERATION:
			operation();
			break;
		case COMPLETE:
			complete();
			break;
		case CLEANUP:
			cleanUp();
			break;
		default:
			break;
		}

	}

	private void check() {
		if(debug.size() > 0){
			for(int i = 0; i < debug.size(); i++){
				debug.get(i).sendMessage("Checking vote factors");
			}
		}

		
		long filler = mainWorld.getTime();
		phase = Phases.INIT;
		long secondsLeft = (cooldown / 1000) + 180 - (System.currentTimeMillis() / 1000);
		if (secondsLeft > 0) {
			player.sendMessage(ChatColor.GOLD + "There is a cooldown, you must wait " + secondsLeft + " more seconds");
			phase = Phases.OFF;
			if(debug.size() > 0){
				for(int i = 0; i < debug.size(); i++){
					debug.get(i).sendMessage("Cooldown, check failed");
				}
			}
		}
		else if(filler < 100 || filler < 12000) {
			player.sendMessage(ChatColor.GOLD + "You can not start a vote, it is not night time");
			phase = Phases.OFF;
			if(debug.size() > 0){
				for(int i = 0; i < debug.size(); i++){
					debug.get(i).sendMessage("Check failed");
				}
			}
		}
		else{
			if(debug.size() > 0){
				for(int i = 0; i < debug.size(); i++){
					debug.get(i).sendMessage("Check passed");
				}
			}
		}
		
		//Bukkit.broadcastMessage(ChatColor.GOLD + "Check out /sleep help, you can skip night, get rewards for sleeping and more");
		plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
	} 

	private void init() {
		if(debug.size() > 0){
			for(int i = 0; i < debug.size(); i++){
				debug.get(i).sendMessage("Starting Initialization");
			}
		}
		yes.clear();
		no.clear();
		yes.add(player);
		countDown = 20;

		filler = Bukkit.getOnlinePlayers().size() - 1;
		phase = Phases.OPERATION;
		bossBar = Bukkit.createBossBar("Yes: " + yes.size() + " No: " + no.size() + " Remaining: " + filler, BarColor.PURPLE, BarStyle.SOLID);
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			if(mainWorld.getPlayers().contains(player)){
				bossBar.addPlayer(player);
			}
		}
		plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
	}

	private void operation() {
		countDown--;
		if(debug.size() > 0){
			for(int i = 0; i < debug.size(); i++){
				debug.get(i).sendMessage("Timer begins: " + countDown + " seconds left");
			}
		}
		
		if (yes.size() + no.size() == Bukkit.getOnlinePlayers().size()) {
			phase = Phases.COMPLETE;
		}
		if (countDown < 1) {
			phase = Phases.COMPLETE;
		}
		filler = Bukkit.getOnlinePlayers().size() - yes.size() - no.size();
		bossBar.setTitle("Yes: " + yes.size() + " No: " + no.size()  + " Remaining: " + filler);
		plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
		bossBar.setProgress((double) countDown / 20);
	}

	private void complete() {
		if(debug.size() > 0){
			for(int i = 0; i < debug.size(); i++){
				debug.get(i).sendMessage("Vote finished");
			}
		}
		countDown = 0;
		bossBar.setProgress(1.0);
		if (yes.size() >= no.size()) {
			
			for(int i = 0; i < prizes.size(); i++){
				prizeSystem(prizes.get(i));
			}
			mainWorld.setTime(0);
			mainWorld.setWeatherDuration(0);
			bossBar.setTitle("Vote Passed");
			bossBar.setColor(BarColor.GREEN);
		} else {

			bossBar.setTitle("Vote Failed");
			bossBar.setColor(BarColor.RED);
			
		}
		cooldown = System.currentTimeMillis();
		phase = Phases.CLEANUP; 
		plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
	}
		
		private void cleanUp(){
			if(debug.size() > 0){
				for(int i = 0; i < debug.size(); i++){
					debug.get(i).sendMessage("Cleaning up, resetting values");
				}
			}
		bossBar.removeAll();
		bossBar = null;
		yes.clear(); 
		no.clear();
		prizes.clear();
		phase = Phases.OFF;
	}
		
	public void prizeSystem(Player target){
		if(debug.contains(target)){
			target.sendMessage("Prize sequence started");
		}
		if(rand.nextInt(5) == 0){
			ItemStack item = new ItemStack(Material.PHANTOM_MEMBRANE);
			target.getInventory().addItem(item);
			if(debug.contains(target)){
				target.sendMessage("Won MEMBRANE");
			}
		}
		if(rand.nextInt(5) == 0){
			ItemStack item = new ItemStack(Material.PRISMARINE_CRYSTALS);
			target.getInventory().addItem(item);
			if(debug.contains(target)){
				target.sendMessage("Won CRYSTAL");
			}
		}
		if(rand.nextInt(5) == 0){
			double temp = target.getHealth();
			if(temp > 18){target.setHealth((double) 20);}
			else{
				target.setHealth(temp + 2);
			}
			if(debug.contains(target)){
				target.sendMessage("Won HEALTH");
			}
		}
	}
}
