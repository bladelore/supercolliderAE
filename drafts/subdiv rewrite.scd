s.boot

Pdef.clear;
t = TempoClock.new(120/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

(
~arrayDepth = { |array|
    var depths;
    if (array.isKindOf(Array)) {
        depths = array.collect { |elem|
            if (elem.isKindOf(Array)) {
                ~arrayDepth.value(elem) + 1;
            } {
                1;
            }
        };
        depths;
    } {
        0;
    }
};


~subArrSize = { |array|
	var size;
	size = array.size;

	array.do { |elem, i|
		if (elem.isKindOf(Array)) {
			size = ~subArrSize.value(elem) * size;
		}
	};

	size;
};

~subPatternDepth = { |array|
    var depths, size;
	array = array.list;
    if (array.isKindOf(Array)) {
        depths = array.collect { |elem, i|
            if (elem.class == Ppatlace) {
                ~subPatternDepth.value(elem) + 1;
            } {
                1;
            }
        };
		depths.maxItem;
    } {
        0;
    };
};

~subPatternSize = { |pattern|
	var size;
	pattern = pattern.list;
	size = pattern.size;
	pattern.do { |elem, i|
		if (elem.class == Ppatlace) {
			size = ~subPatternSize.value(elem) * size;
		}
	};

	size;
};

~makeLaceSubdivision = { |patt, subdiv|
	var mutePattern, pattSize, subdivSize, pattDepth, numEvents;
	//get size of laced duration pattern
	pattSize = ~subPatternSize.(patt);
	//get size of laced subdiv pattern
	subdivSize = ~subPatternSize.(subdiv);
	//get the max depth of the pattern
	pattDepth = ~subPatternDepth.(patt);
	//allow for zeroes in subdiv pattern to set the event to rest
	mutePattern = subdiv;
	subdiv = Pcollect({|item| if(item == 0){ 1 }{ item }}, subdiv);
	//calculate the number of events in the cycle
	numEvents = subdiv.asStream.nextN(subdivSize).sum * pattDepth;
	//create the stream of mutes
	mutePattern = Pdup(subdiv, mutePattern);
	//create the stream of subdivisions
	subdiv = Pdup(subdiv, subdiv);
	//calculate delta values from subdivision pattern
	Plazy{ ~makeDeltas.() } <>
	//mute if 0 on pattern
	Plazy{ Pfunc({|event| if(event[\mutepattern] == 0)
		{ event[\instrument] = \rest }
		{ event[\instrument] = event[\instrument]}; }
	)} <>
	Pbind(
		//embed data into pattern for ~makeDelta function
		\subdiv, Pn(subdiv , inf),
		\dur, Psubdivide(Pkey(\subdiv), Pn(patt, inf)),
		\groupcycle, Pn(pattSize, inf),
		\numevents, Pn(numEvents, inf),
		\mutepattern, Pn(mutePattern, inf),
	)
};

~filterBeat = { |patt, key, beat, mod|
	var thiskey = key.key;

	Pfunc({ |event|
		var thisEvent;
		//modulo input pattern if set
		if(mod.isNil){
			thisEvent = event[thiskey];
		} {
			thisEvent = event[thiskey].wrap(1, mod);
		};
		//set key to rest if not matching
		if(beat.includes(thisEvent)){ event }{ event[\instrument] = \rest }}
	);
};
)

Pcollect({|item| if(item == 0){ 1 }{ item }}, Pseq([1, 0, 1], inf)).asStream.nextN(3)

(
~makeSubdivision.(
	Pseq([1, 1, 1, 1], 1),
	Pseq([4, 4, 4, 4], 1),
).trace.play(t);
)

(
Pdef(\test,
	// ~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4]) <>
~makeLaceSubdivision.(
	PlaceAll([[1, Rest(0.5)], 1,], inf),
	PlaceAll([4, [0, 1], 2], inf),
)
<>Pbind(\instrument, \default)
).trace. play(t);
)

(
Pdef(\test,
~filterBeat.(key: Pkey(\eventcount), beat:[1, 3, 4]) <>
~makeLaceSubdivision.(
	PlaceAll([[1, 1.5], 1.5, 1, [Rest(0.5), 1]], inf),
	PlaceAll([[4, 0], 1, 0, 2], inf),
)
	// <>Pbind(\instrument, \default)
).trace.play(t);
)

//testing
(
var patternList, len, size, depth;
a = Place([1, 1, 1, [1, 2]], inf);
b = [1, [2, [3, [4, 5]]]];
size = ~subPatternSize.(a);
size.postln;
a.asStream.nextN(size);
)

