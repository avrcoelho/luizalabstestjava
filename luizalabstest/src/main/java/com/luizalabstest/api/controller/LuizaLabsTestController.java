package com.luizalabstest.api.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luizalabstest.api.model.LuizaLabsTestModel;

@RestController
@RequestMapping("/api")
public class LuizaLabsTestController {
	
	private Map<String, LuizaLabsTestModel> parser;
	
	public String openFile() throws FileNotFoundException {
		
		//ler o arquivo de logs
		File file = new File("./src/main/resources/games.log"); 
	    Scanner sc = null;
		try {
			sc = new Scanner(file);
			sc.useDelimiter("\\Z"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sc.next();  
	}
	
	// função para separar as partidas num array
	public String[][] separateMatches(String data) {
		// separa as partidas num array
		String[] matches = data.split("------------------------------------------------------------");
		String[][] dataMatches = new String[matches.length][];
		
		// popula o array com dados das partidas tranformadas in array
		for (int i = 0; i < matches.length; i++) {
			dataMatches[i] = matches[i].split("\n");
		}
		 
		return dataMatches;
	}
	
	// cria o objeto e insere as informações
	public LuizaLabsTestController() throws FileNotFoundException {
		
		String data = this.openFile();
		String[][] separate = this.separateMatches(data);
		
		Pattern patternKills = Pattern.compile("^(([0-9]{1,}:[0-9]{1,})\\sKill:)"); // expressão regular para verificar se é kill
		Pattern patternPlayers = Pattern.compile("^(([0-9]{1,}:[0-9]{1,})\\sClientUserinfoChanged:)"); //expressão regular para verififcar se é player
		Pattern patternKiller = Pattern.compile("^(([0-9]{1,}:[0-9]{1,})\\sKill:\\s([0-9]{1,}\\s){1,}[0-9]{1,}:\\s([a-zA-Z0-9]{1,}\\s){1,}killed)"); //expressão regular para identificar o jogador matador;
		Pattern patternWorld = Pattern.compile("^(([0-9]{1,}:[0-9]{1,})\\sKill:\\s([0-9]{1,}\\s){1,}[0-9]{1,}:\\s<world>\\skilled\\s([a-zA-Z0-9]{1,}\\s){1,}by)"); //expressão regular para identificar o jogador morto pelo <world>
        
		int total_kills = 0;
		int pos = 0;
		String player;
		Integer vlKill;
		Integer kills;
		
		parser = new HashMap<String, LuizaLabsTestModel>();
		ArrayList<Object> players = new ArrayList<Object>();
		HashMap<String, Integer> map = new HashMap<>();
		
		// percorrer o array
		for(int i=0; i < separate.length; i++) {
			// remove as posições que não tem informações uteis		
			if (separate[i].length > 3){
			
				total_kills = 0;	
				players.clear();
				map.clear();
				
				for(int j=0; j < separate[i].length; j++) {
					
					Matcher matcherKills = patternKills.matcher(separate[i][j].trim());
					Matcher matcherPlayers = patternPlayers.matcher(separate[i][j].trim());
					
					// verifica se é player
					if(matcherPlayers.find()) {
						// obtem o nome do play
						Pattern ptPlayer = Pattern.compile ("n\\\\(.*)\\\\t\\\\");
						Matcher mtPlayer = ptPlayer.matcher (separate[i][j]);
						if (mtPlayer.find()) {
							// remove elementos desnecessarios no nome do player
						     player = mtPlayer.group().replaceAll("n\\\\", "").replaceAll("\\\\t\\\\", "");
						     
						     // verifica se jogador existe no array, se não existir adiciona
						     if(Arrays.asList(players).indexOf(player) < 0) {
						    	 players.add(player);	 
						    	 map.put(player, 0);
						     }
						}
					}
					
					 // verifica se é kill
					if(matcherKills.find()) {
						 // faz a soma de kills de cada partida
						total_kills = total_kills + 1;
						
						Matcher matcherKiller = patternKiller.matcher(separate[i][j].trim());
						// verifica se é kill por player
						if(matcherKiller.find()) {
							// obtem o nome do player
							Pattern ptKiller = Pattern.compile ("^(([0-9]{1,}:[0-9]{1,})\\sKill:\\s([0-9]{1,}\\s){1,}[0-9]{1,}:\\s(.*)\\skilled)");
							Matcher mtKiller = ptKiller.matcher (separate[i][j].trim());
							
							if (mtKiller.find()) {
								player = mtKiller.group(4);
								// faz a soma de kills de cada player
								vlKill = map.get(player);
								kills = vlKill + 1;
								map.put(player, kills);
							}
						}
						
						Matcher matcherWorld = patternWorld.matcher(separate[i][j].trim());
						// verifica se é kill por <world>
						if(matcherWorld.find()) {
							// obtem o nome do player
							Pattern ptWord = Pattern.compile ("^(([0-9]{1,}:[0-9]{1,})\\sKill:\\s([0-9]{1,}\\s){1,}[0-9]{1,}:\\s<world>\\skilled\\s(.*)\\sby)");
							Matcher mtWorld = ptWord.matcher (separate[i][j].trim());
							
							if (mtWorld.find()) {
								player = mtWorld.group(4);
								
								vlKill = map.get(player);
								  // verifica se atributo esta com valor maior que 0
								if(vlKill > 0) {
									// subtrai 1
									kills = vlKill - 1;
									map.put(player, kills);
								}
							}
						}
					}
				}
			    
			    LuizaLabsTestModel p1 = new LuizaLabsTestModel(total_kills, players, map);
			    
			    pos++;
			    
			    parser.put("game_"+pos, p1);
			}	
		}
	}
	
	@GetMapping(value = "/")
	public ResponseEntity<HashMap<String, LuizaLabsTestModel>> listar() {
	    return new ResponseEntity<HashMap<String,LuizaLabsTestModel>>(new HashMap<String, LuizaLabsTestModel>(parser), HttpStatus.OK);
	 }
}
