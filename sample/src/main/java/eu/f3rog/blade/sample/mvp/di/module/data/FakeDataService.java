package eu.f3rog.blade.sample.mvp.di.module.data;


import android.content.res.Resources;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import eu.f3rog.blade.sample.mvp.model.Actor;
import eu.f3rog.blade.sample.mvp.model.ActorDetail;
import eu.f3rog.blade.sample.mvp.service.DataService;
import rx.Single;
import rx.SingleEmitter;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/* package */ final class FakeDataService implements DataService {

    @NonNull
    private final List<Actor> mActors;
    @NonNull
    private final static String INFO = "Eos cogitantur sum aliquandiu contingere. Explicui callidus gi profecto actiones addamque collecta si. Mecum signa erant serie novas et ut. Fundamenta mo facultatem ha is diligenter durationis. Aliquam mea aperire scripti dicerem usu erumpam quodque sed. Deceptorem aliquamdiu hac pro praesertim ita. Alios ferre famam agi dat quasi ferri ideis. Quicquam rem diversae concipio sap sessione rom recordor ita. Is ab reddat angeli ex latere realem mendax cetera."
            + "\n\n"
            + "Aestimare perceptio archetypi fruebatur immittant mem hoc iii sic. Rei ita haberem fecisse uno formari mea probari. Angelos ibi ignorem deesset rum lapidis respexi caeteri nul cau. Locis pauci adsit fecto vim voces miror nia. Negari re sequor illico posita notatu quanto ut. Attributa vox cunctatus explorant delusisse instituti nul praestari. Humanae insuper essendi se ejusque to columen positis."
            + "\n\n"
            + "Odoratum diversum vos rerumque ope efficiat hos qua realitas locusque. To ii ex efficiat defectus majorque immorari ad realiter infinite. Mendax perire simile mo ne causae ea co. Ii neque major ob habeo. Quies is omnia ei somni. Omne apti ad rogo novi is omni inde."
            + "\n\n"
            + "Caeteras diversae ha alicujus im at de. Fallit du solius summae ac cumque ea. Is addamque reductis cessarem ac eo et. Eo falleret co ac posuisse insidias. Mei credent gallico usu peccant credere fatigor positis vis. Materia retinet frustra in ac quaenam vi ex. Dari ille agi vim quis. An caelum simile judico magnis moveri ac. Seipsum in quatuor visione deumque ob ex attendo."
            + "\n\n"
            + "Perfectum ii infirmari partiales percipior naturalis ad. Mei una fructum tanquam student existam quietem seorsim aut. Essentiae denegante perceptio distinguo realitate vox hos. Negat mundo neque ii co modos gi. Nec vel non etsi duas rem dari regi adeo casu. Capax seu ausit dum cum fidei sexta. Est suo apud fal quam sola idem lus. In complexus importare praefatio tangantur ac de."
            + "\n\n"
            + "Revocari in sessione me innotuit se gi. Adhuc solum fas porro nec ubi majus non. Autem falso harum si de certo pauca mundo. Quarta at to habeam sequor at nomine deinde. Nam imaginatio formaliter proficisci continetur sua cujuslibet scripturas. Materia proxime ineptum vox tam. Usu digna situs ausit vulgo sed eam. Propter pretium vox insuper sit possunt finitae."
            + "\n\n"
            + "Attigi aetate jam nullis ubi falsae terram realem. Rei jam discrepant iii aliquoties qua falsitatis mortalibus. Re im de terram gi omnium doctus vestro sensum. Etc persuasi monendos duo dedissem mutuatis ens hoc videamus. Fuse his male cap tria tam poni. Ibi nosse sanae agi satis ego ego. Ego dignum lumine illico debere hoc."
            + "\n\n"
            + "Putarem quodque tamquam ii ob an deumque. Fuerint judicia me assequi sapores ab verarum. Veniebant sex videbatur assignare eas una corporeis alligatus. Oculis nondum fusius sub ens urgeat fuerit rea sum. Eo ex deumque reddere publice ea similes attinet. Sensuum ac gi fallant incipit. Nullis carnem tam fal existi haereo mei sacras ipsius. Cap manifestum asseverent agi persuaderi statuendum his complector explicetur. Passim vestes maxime pulses animus id ii. Co facillimam industriam prudentiae ii quaerendum scripturas ab at aliquoties."
            + "\n\n"
            + "Usitate deo dicitur ibi seorsim ubi. Ageretur sum mutuatur acceptis meditari qua loquebar. Restat dicunt postea ac si capram me primam. Cogitare ad ex ab gradatim tractare et. Tes vox persuasi vel nihildum impellit effectus. Fallit dubias liceat qualem ii ut. Acquiro liquida hominem de viderer an pugnare ut ac. Vigilantes necessario ex divisibile intellectu co in cohibendam necessitas re."
            + "\n\n"
            + "Dubitem videmur usitate itemque ob afferri de. Ac in obfirmata existeret at devenimus ii affirmare. Sic ullos vel fidam sequi quasi. Unde fiat iis hos sunt tot meas quum. Ima corporibus tot efficiente sufficeret non viderentur distinctae vos. Ideo dei quis pla sive haec foco fore. Mox otii aspi iste imo hos ipsi. Earumdem videatur eam hae sui agnoscam. Creatione co corporeis consistat at conservat corporeas. Imo nul pro rom talis sonos fieri.";

    public FakeDataService() {
        mActors = generateUsers();
    }

    @NonNull
    @Override
    public Single<List<Actor>> getAllActors() {
        return Single.just(mActors)
                .delay(new Random().nextInt(5), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    @Override
    public Single<ActorDetail> getActorDetail(final long id) {
        return Single.fromEmitter(
                new Action1<SingleEmitter<ActorDetail>>() {
                    @Override
                    public void call(SingleEmitter<ActorDetail> emitter) {
                        Actor actor = null;

                        final List<Actor> actors = new ArrayList<>(mActors);
                        for (final Actor a : actors) {
                            if (id == a.getId()) {
                                actor = a;
                                break;
                            }
                        }

                        if (actor == null) {
                            emitter.onError(new Resources.NotFoundException("Actor not found"));
                        } else {
                            emitter.onSuccess(new ActorDetail(actor.getName(), "1.1.1990 in Fake city", INFO));
                        }
                    }
                })
                .delay(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    private List<Actor> generateUsers() {
        final String[] names = new String[]{
                "Elfrieda Hutchcraft",
                "Shane Gilmour",
                "Arthur Reep",
                "Minna Keirn",
                "Nicki Riner",
                "Kiyoko Campanella",
                "Regan Gasper",
                "Latosha Peel",
                "Sabra Stefanski",
                "Ja Royster",
                "Debora High",
                "Jutta Fredricks",
                "Mariana Mangan",
                "Teresa Nantz",
                "Renea Reeb",
                "Damon Roseman",
                "Arnetta Higgenbotham",
                "Laverne Talbott",
                "Royce Coppock",
                "Joey Mcchristian",
                "Tara Nedd",
                "Dominic Gossard",
                "Meghan Brucker",
                "Sabrina Marine",
                "Blake Burnett",
                "Valerie Suydam",
                "Jennette Bunce",
                "Winnie Marie",
                "Caprice Siegler",
                "Janine Schmalz",
                "Lane Mcmurry",
                "Lessie Vetrano",
                "Chanell Bosket",
                "Blossom Tynes",
                "Augustine Broadbent",
                "Carli Branson",
                "Kasi Sletten",
                "Sabine Lipton",
                "Louisa Emmert",
                "Robbi Mcglynn",
                "Alvaro Mae",
                "Ora Manigo",
                "Buster Campfield",
                "Trinity Muldrew",
                "Jonathan Groat",
                "Vania Perlmutter",
                "Arica Clutts",
                "Alexander Lacasse",
                "Zulma Aasen",
                "Adria Beatrice"
        };

        final List<Actor> actors = new ArrayList<>(names.length);

        for (int i = 0; i < names.length; i++) {
            actors.add(new Actor(i, names[i]));
        }

        return actors;
    }
}
