#!/usr/bin/python3

import pandas as pd
import math


exclude = ["Orlová", "Havířov", "Frýdek-Místek", "Litvínov", "Břevnov", "Opava", "Český Těšín", "Brandýs nad Labem-Stará Boleslav", "Jirkov", "Kralupy nad Vltavou", "Kopřivnice", "Valašské Meziříčí"]
links = [["Hradec Králové", "Brno"], ["Hradec Králové", "Šumperk"], ["Prague", "Tábor"], ["Prague", "Kolín"]]
exclude_links = [["Chomutov", "Litoměřice"], ["Havlíčkův Brod", "Hradec Králové"], ["Kladno", "Písek"], ["Příbram", "České Budějovice"], ["Česká Lípa", "Jablonec nad Nisou"], ["Trutnov", "LIberec"], ["Břeclav", "Uherské Hradiště"], ["Hodonín", "Zlín"], ["Vsetín", "Kroměříž"], ["Třinec", "Český Těšín"], ["Most", "Karlovy Vary"], ["Příbram", "Tábor"], ["Přerov", "Uherské Hradiště"], ["Písek", "Havlíčkův Brod"], ["Litoměřice", "Příbram"], ["Chomutov", "Cheb"], ["Písek", "Jihlava"], ["Trutnov", "Liberec"], ["Pardubice", "Trutnov"], ["Hodonín", "Přerov"], ["Přerov", "Prostějov"], ["Kutná Hora", "Hradec Králové"], ["Chrudim", "Hradec Králové"], ["Pardubice", "Náchod"], ["Karviná", "Ostrava"], ["Pardubice", "Žďár nad Sázavou"], ["Klatovy", "Beroun"], ["Beroun", "Mělnik"], ["Písek", "Klatovy"], ["Chrudim", "Náchod"], ["Cheb", "Karlovy Vary"], ["Sokolov", "Žatec"], ["Nový Jičín", "Bohumín"], ["Vsetín", "Bohumín"], ["Vyškov", "Přerov"], ["Zlín", "Vyškov"], ["Vyškov", "Olomouc"], ["Náchod", "Jablonec nad Nisou"], ["Most", "Žatec"], ["Blansko", "Vyškov"], ["Vyškov", "Zlín"], ["Ústí nad Labem", "Louny"]]

cities = pd.read_csv("cz.csv")[:70]
cities = cities[~cities["city"].isin(exclude)]
cities["lat"] *= 1.5
tmp = {}

print(f"DELETE FROM LINKS;")
print(f"DELETE FROM NODES;")

for i, city in cities.iterrows():
	name = city["city"]
	lng = city["lng"]
	lat = city["lat"]
	id = i + 1
	tmp[name] = {"lng": lng, "lat": lat, "id": id, "links": [], "size": (int(math.log(city["population"], 10)) - 3) * 2, "name": name}

	print(f"INSERT INTO NODES VALUES ({id}, '{name}', SDO_GEOMETRY(2001, NULL, SDO_POINT_TYPE({lng}, {lat}, NULL), NULL, NULL));")


def get_dist(start, end):
	return math.sqrt((start["lng"] - end["lng"])**2 + (start["lat"] - end["lat"])**2)

def is_excluded_link(start, end):
	for link in exclude_links:
		if start in link and end in link:
			return True

	return False

def get_nearest(start, n):
	nearests = []

	for j in range(n):
		nearest = None
		dist = 10e10

		for i, city in cities.iterrows():
			name = city["city"]
			d = get_dist(start, tmp[name])

			if d < dist and d > 0 and tmp[name]["id"] not in start["links"] and not is_excluded_link(start["name"], name):
				dist = d
				nearest = tmp[name]

		if nearest:
			nearests.append(nearest)
			start["links"].append(nearest["id"])
			nearest["links"].append(start["id"])

	return nearests

link_id = 0

for i, city in cities.iterrows():
	name = city["city"]
	curr = tmp[name]
	nearests = get_nearest(curr, curr["size"])
	from_id = curr["id"]

	for to in nearests:
		to_id = to["id"]
		link_id += 1
		print(f"INSERT INTO links VALUES ({link_id}, {from_id}, {to_id}, SDO_GEOMETRY(2002, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1), SDO_ORDINATE_ARRAY({curr['lng']},{curr['lat']}, {to['lng']},{to['lat']})));")


for link in links:
	start = tmp[link[0]]
	end = tmp[link[1]]
	link_id += 1
	print(f"INSERT INTO links VALUES ({link_id}, {start['id']}, {end['id']}, SDO_GEOMETRY(2002, NULL, NULL, SDO_ELEM_INFO_ARRAY(1, 2, 1), SDO_ORDINATE_ARRAY({start['lng']},{start['lat']}, {end['lng']},{end['lat']})));")

