///COMPOSTION FUNCTIONS
(
//to do
//metric modulation
//accel/decel by phase?
//swing/swing by group
//interpolate between patterns by over x bars
//choke groups
//add risers to hits
//sequence sections
//delta to

~pFade = { |a, b, dur, curve='linear', loop=0|
	var env;
	var chance = Pwhite(0.0, 1.0, inf).asStream;

	if(loop == 1)
	{ env = Pn(Pseg(Pseq([0 + 1e-4, 1, 0 + 1e-4], 1), dur, curve, inf)).asStream; }
	{ env = Env.new([0 + 1e-6, 1], dur, curve).asStream; };

	env.trace;

	Ppar([
		Pbindf(a, \instrument,
			Pfunc({|ev|
				var val;
				if((ev[\type] == 'monoNote').or(ev[\type] == 'monoSet'))
				{
					val = ev[\instrument];
					ev[\amp] = 1 - env.next;
				} {
					if(chance.next < env.next)
					{ val = \rest; }
					{ val = ev[\instrument]};
				};
				val;
			})
		),

		Pbindf(b, \instrument,
			Pfunc({|ev|
				var val;
				if((ev[\type] == 'monoNote').or(ev[\type] == 'monoSet'))
				{
					val = ev[\instrument];
					ev[\amp] = env.next;
				} {
					if(chance.next > env.next)
					{ val = \rest; }
					{ val = ev[\instrument]};
				};
				val;
			})
		)
	]);
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

~makeSubdivision = { |patt, subdiv|
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

~appendRest = { |pattern, pDur, restDur|
	Pseq([pattern.finDur(pDur), Rest(restDur)], inf);
};


~makeDeltas = { |patt|
		var eventcount = 0;
		var groupcount = 0;
		var cyclecount = 0;
		Pbind(
			//delta time in relation to tempo clock
			\bardelta, (Ptime() % t.beatsPerBar) / t.beatsPerBar,
			//janky ass subroutine
			//group delta time
			\groupdelta, Pfunc({|event|
				var groupdelta, cycledelta;
				//event index in group
				event[\eventcount] = eventcount + 1;
				eventcount = eventcount + 1;
				//as delta within group
				groupdelta = (eventcount - 1) / event[\subdiv];

				//index of group
				if(groupcount == event[\groupcycle]){
					groupcount = 0;
				};

				event[\groupcount] = groupcount + 1;
				if(eventcount == event[\subdiv]){
					eventcount = 0;
					groupcount = groupcount + 1;
				};

				//add cycle count
				event[\cyclecount] = cyclecount + 1;
				cyclecount = cyclecount + 1;
				event[\cycledelta] = (cyclecount - 1) / event[\numevents];

				if(cyclecount == event[\numevents]){
					cyclecount = 0;
				};
				//final return
				groupdelta;
			}),
		)
};

~nearestDelta = { |patt, key, downbeat, centre|
	//wtf does this do tbh
	abs(round(centre - key, downbeat) - (centre - downbeat));
};

SynthDef(\modenvmono, { arg out=0, amp=0.1, gate=1;
	var sig;
	sig = EnvGen.kr(\env.kr(Env([1,1],[0.1])), \itrig.tr(1), doneAction:0);
	sig = sig * EnvGen.kr(Env([1,1,1],[0.1,0.8], releaseNode:1), gate, doneAction:2);
	Out.kr(out, sig);s
}).add;

~pmodenv = { arg valpat, timepat=1, repeat=1, curvepat;
	// return a pattern that append a function to \finish which start a Pmono of modenvmono synth outputing an env curve according to patterns from function parameters
	repeat = repeat.clip(1,inf); // prevent infinite loop when puting negative curve in repeat
	Prout({ arg ev;
		var bus = Bus.control(s, 1);
		var timestr;
		var running = true;
		var cleanup = EventStreamCleanup.new;
		var patplayer;
		var finish_fun;
		var cleanup_fun = {
			patplayer.stop;
			running = false;
			//"pmodenv: CLEANUP".debug;
			{
				2.wait;
				if(bus.index.notNil) {
					bus.free;
				}
			}.fork;
		};

		timepat = timepat ??  { Plazy({ ev[\dur] }).loop };
        timepat = Pchain(timepat, Pfunc({ ev }));
		curvepat = curvepat ??  { 0 };
        curvepat = Pchain(curvepat, Pfunc({ ev }));
        valpat = Pchain(valpat, Pfunc({ ev }));

		cleanup.addFunction(ev, cleanup_fun);

		CmdPeriod.doOnce({
			if(bus.index.notNil) {
				bus.free
			}
		});

		finish_fun = {
			patplayer = Pmono(\modenvmono,
				\out, bus,
				\itrig, 1,
				[ \dur, \env ], Prout({ arg monoev;
					var valstr = valpat.asStream;
					var curvestr = curvepat.asStream;
					var previous = valstr.next;
					var time;
					timestr = timepat.asStream;

					block { arg break;
						valstr.do { arg val;
							var prev = previous;
							var curve;
							//val.debug("pmodenv val");
							time = timestr.next;
							curve = curvestr.next;
							if(time.isNil) {
								time = 2;
								monoev[\dur] = time;
								break.value;
							};
							if(curve.isNil) {
								time = 2;
								monoev[\dur] = time;
								break.value;
							};

							//monoev[\dur] = time;
							//Env([prev,val],[time]/thisThread.clock.tempo).asCompileString.debug("env");
							monoev = [time, [ Env([prev,val],[time]/t.tempo, curve) ]].yield;
							// monoev = [time, [ Env([prev,val],[time]/thisThread.clock.tempo, curve) ]].yield;

							previous = val;
						};
					};
					running = false;
					monoev;
				}),
				\legato, 1,
			).play;
		};

		if(ev[\finish].isKindOf(Function)) {
			var oldfun = ev[\finish];
			ev[\finish] = { finish_fun.value; oldfun.value };
		} {
			ev[\finish] = finish_fun;
		};

		while{ running == true } {
			//"pmodenv running".debug;
			var ret = bus.asMap;
			cleanup.update(ev);
			ev = ret.yield;
			// ev.debug("ev");
			if(ev.isNil) {
				"pmodenv: pattern ends".debug; // this never happen
				cleanup_fun.value;
			}
		};
		ev;
	}).repeat(repeat)
};

)
