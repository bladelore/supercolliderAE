Require("/Users/aelazary/sc/startup.scd")

PbindGenerator(\fb1, false, true)

(
Pdef('swinger', 
	Pbind(
		\instrument, \kick2,
		\freq, 55.0,
		\sweep, 2.0,
		\roomsize, 1.0,
		\reverbtime, 1.0,
		\atk, 0.03999999910593,
		\dec, 0.20000000298023,
		\gain, -15.0,
		\pan, 0.0,
		\amp, 2.0,
	).withSwing(0.25, 0.5)
).play(t)
)



(
	~arrayDelta = {|array|
		var cumSum = Array.fill(0, array.size+1);
		var sum = 0.0;
	
		(array.size - 1).do {|i|
			sum = sum + cumSum[i+1];
			cumSum[i]=sum;
		};
	
		// cumSum = cumSum.asArray; 
		// cumSum.removeAt(cumSum.size-1);
	};
)


(
	~arrayDelta = {|array|
		var cumSum = List.new;
		var sum = 0.0;
		var arrSize;
	
		cumSum.add(0);
	
		array.do { |item, i|
			sum = sum + item;
			cumSum.add(sum);
		};
	
		cumSum = cumSum.asArray; 
		cumSum.removeAt(cumSum.size-1);
		cumSum
	};

	~eventSkew = {|patt, idx=1, skew=0, min=0.1, curve=\lin, numEvents, eventArr|
		var durList, durSize, durSum;
		var env, skewArr, newDur, newDelta;
		
		durList = List.new;
	
		//get durations as array for group
		numEvents.do({|i|
			var thisKey = eventArr[i].at('groupcount');
			var thisDur = eventArr[i].at('dur');
			if(thisKey == idx){
				durList.add(thisDur);
			};
		});
	
		durList = durList.asArray;
		durSize = durList.size;
		
		//skew range -1..1
		skewArr = if(skew >= 0){[skew, min]}{[min, abs(skew)]};
	
		env = Env(skewArr, [durSize], curve);
		//(durations[n] * sampled envelope[n]) normalised between 1 / scaled to duration of group
		newDur = (durList * env.at((0..durSize-1))).normalizeSum * durList.sum;
		newDelta = ~arrayDelta.(newDur);
		
		// newDelta.postln;
		newDur.postln;
		newDur.sum.postln;

		//replace events at group with skewed rhythm
		Pfunc({|ev|
			if((ev[\groupcount] == idx) && (ev[\groupcount] == durSize)) 
			{ 	
				var thisDur = newDur[ev[\eventcount] - 1];
				thisDur.postln;
				ev[\eventcount].postln;
				ev[\dur] = thisDur;
				// ev[\groupdelta] = newDelta[ev[\eventcount] - 1];
			} 
			{ev = ev}
		});
	};

	~cycleSkew = {|patt, skew=0, min=0.1, curve=\lin, numEvents, eventArr|
		var durList, durSize, durSum;
		var env, skewArr, newDur;
		
		durList = List.new;
	
		//get durations as array for group
		numEvents.do({|i|
			var thisDur = eventArr[i].at('dur');
			durList.add(thisDur);
		});
	
		durList = durList.asArray;
		durSize = durList.size;
		
		//skew range -1..1
		skewArr = if(skew >= 0){[skew, min]}{[min, abs(skew)]};
	
		env = Env(skewArr, [durSize], curve);
		//(durations[n] * sampled envelope[n]) normalised between 1 / scaled to duration of group
		newDur = (durList * env.at((0..durSize-1))).normalizeSum * durList.sum;
		
		//replace events at group with skewed rhythm
		Pfunc({|ev|
			ev[\dur] = newDur[ev[\cyclecount] - 1];
		});
	};

	~pSkew = {|patt, key, group=1, skew=0, min=0.1, curve=\lin|
		var chain, chainList;
		var numEvents, eventArr;
		var arrSkew, arrCurve;
		var thisKey = key.key;
		
		//get total number of events
		numEvents = Pn(patt.value, inf).asStream.next(()).asDict.at('numevents');
		numEvents.postln;
		//get array of events from pattern
		eventArr = patt.value.asStream.nextN(numEvents,());

		chainList = List.new;

		case{thisKey=='eventcount'}
		{
			if(group.isKindOf(Array)){
				// skew for each group idx in passed array
				group.size.do({|i|
					//if array, wrap array values for other params
					if(skew.isKindOf(Array)){ arrSkew = skew[i.wrap(0, skew.size - 1)]}{arrSkew = skew};
					if(curve.isKindOf(Array)){ arrCurve = curve[i.wrap(0, curve.size - 1)]}{arrCurve = curve};
					//add to list
					chainList.add(~eventSkew.(patt, group[i], arrSkew, min, arrCurve, numEvents, eventArr));
				});
				//expand list, applying function to each group
				chain = Pchain(*chainList);
			} {
				chain = ~eventSkew.(patt, group, skew, min, curve, numEvents, eventArr);
			}
		}

		{thisKey=='cyclecount'}
		{
			if(skew.isKindOf(Array)){ arrSkew = skew[0]}{arrSkew = skew};
			if(curve.isKindOf(Array)){ arrCurve = curve[0]}{arrCurve = curve};

			chain = ~cycleSkew.(patt, arrSkew, min, arrCurve, numEvents, eventArr);
		}
	};
)

(
t = TempoClock.new(160/60).permanent_(true).schedAbs(0, {t.beatsPerBar_(4)});

// Pdef(\p1,
// 	~makeSubdivision.(
// 		PlaceAll([[1.5, 1], [1.5, 1], [1,2]], inf),
// 		// PlaceAll([1.5, 1.5, 2], inf),
// 		PlaceAll([4, 4, 4, 3, 5], inf)
// 	)
// );

Pdef(\p1,
	~makeSubdivision.(
		PlaceAll([1, 1, 1, [1, 0.5]] * 2, inf),
		// PlaceAll([1.5, 1.5, 2], inf),
		PlaceAll([4, 3, 3, 3], inf)
	)
);

Pdef(\skewed,
	Pbind(
		\freq, 55.0,
		\sweep, 4,
		\roomsize, 1+(Pkey(\groupdelta)*4),
		\reverbtime, 1+(Pkey(\groupdelta)*4),
		\atk, Pkey(\groupdelta).linlin(0,1, 0.05, 0.3),
		\dec, 0.20000000298023,
		\gain, -6.0,
		\pan, 0.0,
		\amp, 1.0,
	) <>
	~pSkew.(Pdef(\p1), key: Pkey(\eventcount), idx: 4, skew: 2, curve: \lin) <> 
	// ~filterBeat.(key: Pkey(\cyclecount), beat:[1, 2, 4], mod: 3) <>
	// ~pSkew.(Pdef(\p1), key: Pkey(\eventcount), idx: [1, 3], skew: [2, 1], curve: \exp) <> 
	// ~pSkew.(Pdef(\p1), key: Pkey(\cyclecount), idx: 1, skew: 1, curve: \exp) <> 
	Pdef(\p1)
	<> Pbind(
		\instrument, \kick2,
	)
).play(t);
)


(
	Pdef(\p1,
		// Pbind(\degree, Pkey())
		~makeSubdivision.(
			PlaceAll([[1, 1], 1, 1], inf),
			// PlaceAll([1.5, 1.5, 2], inf),
			PlaceAll([3, 1], inf)
		)
	).trace(['groupcycleLCM']).play(t)
)

lcm(6,2)
~lcmIndices.(2,6);

(
	~lcmIndices = {|num, denom|
		var lcm = lcm(3, 4);
		var lcmSeq = (0..(lcm-1));
		var indices = (0..(denom-1)) * num;
		lcmSeq.at(indices);
	};

	~lcmIndices.([4,3],3);
)

lcm(lcm(4,4),1);