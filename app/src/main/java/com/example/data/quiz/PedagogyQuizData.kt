package com.example.data.quiz

import com.example.model.quiz.QuizCategory
import com.example.model.quiz.QuizModule
import com.example.model.quiz.QuizOption
import com.example.model.quiz.QuizQuestion

object PedagogyQuizData {

    val allModules: Map<QuizCategory, QuizModule> = mapOf(
        QuizCategory.ANRUFEN to QuizModule(
            category = QuizCategory.ANRUFEN,
            title = "Telefonieren & Anrufen",
            childIntroText = "Mit dem Telefon kannst du Familie und Freunde anrufen und sprechen.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_call_1",
                    title = "Mama anrufen",
                    situation = "Du möchtest deine Mama anrufen. Was machst du?",
                    options = listOf(
                        QuizOption("A", "Du suchst Mama in deinen Kontakten."),
                        QuizOption("B", "Du rufst irgendeine Nummer an."),
                        QuizOption("C", "Du wartest, bis Mama dich anruft."),
                        QuizOption("D", "Du schreibst die Nummer auf einen Zettel.")
                    ),
                    correctIndex = 0,
                    explanation = "Am sichersten ist es, eine bekannte Person über den richtigen Kontakt anzurufen."
                ),
                QuizQuestion(
                    id = "c_call_2",
                    title = "Aus Versehen angerufen",
                    situation = "Du hast jemanden aus Versehen angerufen. Was kannst du machen?",
                    options = listOf(
                        QuizOption("A", "Immer wieder anrufen."),
                        QuizOption("B", "Auflegen und später eine Nachricht schreiben."),
                        QuizOption("C", "Das Handy ausschalten."),
                        QuizOption("D", "Die Person blockieren.")
                    ),
                    correctIndex = 1,
                    explanation = "Ein versehentlicher Anruf ist kein Problem. Du kannst auflegen und dich gegebenenfalls per Nachricht erklären."
                ),
                QuizQuestion(
                    id = "c_call_3",
                    title = "Niemand hebt ab",
                    situation = "Du rufst jemanden an, aber die Person geht nicht ran. Was kannst du tun?",
                    options = listOf(
                        QuizOption("A", "20-mal hintereinander anrufen."),
                        QuizOption("B", "Eine Nachricht schreiben und später noch einmal versuchen."),
                        QuizOption("C", "Das Handy der Person kaputtmachen."),
                        QuizOption("D", "Die Nummer löschen.")
                    ),
                    correctIndex = 1,
                    explanation = "Die andere Person kann gerade beschäftigt sein. Eine Nachricht oder ein späterer Anruf reicht."
                )
            ),
            childSummaryPoints = listOf(
                "Bekannte Personen immer über gespeicherte Kontakte anrufen.",
                "Ein versehentlicher Anruf ist nicht schlimm: Einfach auflegen und Bescheid sagen.",
                "Wenn niemand rangeht: Geduld haben, später versuchen oder eine kurze Nachricht schreiben."
            ),
            parentIntroText = "Beim Telefonieren lernt Ihr Kind, verantwortungsvoll mit Kontakten umzugehen und Gesprächsregeln zu beachten.",
            parentFocusTopics = listOf(
                "Nur gespeicherte Kontakte anrufen",
                "Umgang mit versehentlichen Anrufen",
                "Verständnis für Nichterreichbarkeit",
                "Feste Ansprechpartner vereinbaren"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_call_1",
                    title = "Kontakte anrufen",
                    situation = "Ihr Kind möchte jemanden aus seinen Kontakten anrufen. Was ist eine sinnvolle Unterstützung?",
                    options = listOf(
                        QuizOption("A", "Das Kind soll möglichst über gespeicherte Kontakte anrufen und bei Unsicherheit nachfragen."),
                        QuizOption("B", "Das Kind soll irgendeine Nummer ausprobieren."),
                        QuizOption("C", "Das Kind soll Telefonnummern aus dem Internet verwenden."),
                        QuizOption("D", "Das Kind soll seine Telefonnummer an unbekannte Personen weitergeben.")
                    ),
                    correctIndex = 0,
                    explanation = "Bekannte Kontakte und Nachfragen bei Unsicherheit unterstützen einen sicheren Umgang mit Anrufen."
                ),
                QuizQuestion(
                    id = "p_call_2",
                    title = "Versehentlicher Anruf",
                    situation = "Ihr Kind hat versehentlich jemanden angerufen. Wie reagieren Sie sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Das Kind dafür bestrafen."),
                        QuizOption("B", "Gemeinsam überlegen, ob eine kurze Nachricht zur Erklärung sinnvoll ist."),
                        QuizOption("C", "Die Person sofort zurückrufen und beschimpfen."),
                        QuizOption("D", "Das Telefonieren grundsätzlich verbieten.")
                    ),
                    correctIndex = 1,
                    explanation = "Ein versehentlicher Anruf ist normalerweise kein großes Problem. Das Kind kann lernen, ruhig damit umzugehen."
                ),
                QuizQuestion(
                    id = "p_call_3",
                    title = "Niemand erreichbar",
                    situation = "Ihr Kind erreicht jemanden telefonisch nicht. Was ist eine gute Regel?",
                    options = listOf(
                        QuizOption("A", "So lange anrufen, bis jemand abnimmt."),
                        QuizOption("B", "Eine kurze Nachricht hinterlassen und später noch einmal versuchen."),
                        QuizOption("C", "Die Person über andere Nummern suchen."),
                        QuizOption("D", "Die Nummer öffentlich posten.")
                    ),
                    correctIndex = 1,
                    explanation = "Menschen müssen nicht jederzeit erreichbar sein. Eine Nachricht und ein späterer Versuch reichen meist aus."
                )
            ),
            parentTips = listOf(
                "Wichtige Nummern als Favoriten mit Foto speichern.",
                "Erklären, dass Daueranrufe andere stören.",
                "Kind bestärken, bei Unsicherheit nachzufragen."
            )
        ),

        QuizCategory.UNBEKANNTE_NUMMERN to QuizModule(
            category = QuizCategory.UNBEKANNTE_NUMMERN,
            title = "Unbekannte Rufnummern",
            childIntroText = "Manchmal rufen Nummern an, die du nicht kennst. Hier lernst du, wie du dich schützt.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_unk_1",
                    title = "Unbekannter Anrufer",
                    situation = "Eine Nummer, die du nicht kennst, ruft dich an. Was ist eine gute Idee?",
                    options = listOf(
                        QuizOption("A", "Sofort zurückrufen."),
                        QuizOption("B", "Die Nummer erst prüfen oder einen Erwachsenen fragen."),
                        QuizOption("C", "Deine Adresse nennen."),
                        QuizOption("D", "Die Nummer an Freunde schicken.")
                    ),
                    correctIndex = 1,
                    explanation = "Bei unbekannten Nummern musst du nicht sofort reagieren. Prüfe die Nummer oder frage eine Vertrauensperson."
                ),
                QuizQuestion(
                    id = "c_unk_2",
                    title = "Adresse erfragt",
                    situation = "Eine unbekannte Person sagt am Telefon: „Sag mir schnell deine Adresse.“ Was machst du?",
                    options = listOf(
                        QuizOption("A", "Ich sage meine Adresse."),
                        QuizOption("B", "Ich frage nach, warum die Person sie braucht."),
                        QuizOption("C", "Ich gebe die Adresse nicht heraus und hole mir Hilfe."),
                        QuizOption("D", "Ich sage meine Schuladresse.")
                    ),
                    correctIndex = 2,
                    explanation = "Eine Adresse ist eine persönliche Information. Gib sie unbekannten Personen nicht einfach heraus und hole dir Hilfe."
                ),
                QuizQuestion(
                    id = "c_unk_3",
                    title = "Wiederholte Anrufe",
                    situation = "Eine unbekannte Nummer ruft dich immer wieder an. Was kannst du machen?",
                    options = listOf(
                        QuizOption("A", "Immer zurückrufen."),
                        QuizOption("B", "Die Nummer blockieren und einem Erwachsenen Bescheid sagen."),
                        QuizOption("C", "Die Nummer öffentlich posten."),
                        QuizOption("D", "Deine Telefonnummer zurückschicken.")
                    ),
                    correctIndex = 1,
                    explanation = "Wenn Anrufe unerwünscht sind, kannst du die Nummer blockieren und eine erwachsene Vertrauensperson informieren."
                )
            ),
            childSummaryPoints = listOf(
                "Unbekannte Nummern nicht voreilig zurückrufen.",
                "Niemals die Adresse oder private Infos am Telefon verraten.",
                "Störende Nummern sofort blockieren und Eltern informieren."
            ),
            parentIntroText = "Unerwünschte Werbe- oder Spam-Anrufe verunsichern Kinder. Gemeinsame Regeln geben Sicherheit.",
            parentFocusTopics = listOf(
                "Keine privaten Daten an Fremde",
                "Gemeinsames Prüfen verdächtiger Nummern",
                "Konsequentes Blockieren"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_unk_1",
                    title = "Umgang mit unbekannten Anrufern",
                    situation = "Ihr Kind erhält einen Anruf von einer unbekannten Nummer. Was ist eine sinnvolle Regel?",
                    options = listOf(
                        QuizOption("A", "Sofort zurückrufen."),
                        QuizOption("B", "Die Nummer prüfen und bei Unsicherheit eine erwachsene Vertrauensperson einbeziehen."),
                        QuizOption("C", "Zuerst die Adresse nennen."),
                        QuizOption("D", "Die Nummer in einer Gruppe veröffentlichen.")
                    ),
                    correctIndex = 1,
                    explanation = "Bei unbekannten Nummern sollte nicht vorschnell reagiert oder persönliche Information herausgegeben werden."
                ),
                QuizQuestion(
                    id = "p_unk_2",
                    title = "Forderung nach Daten",
                    situation = "Eine unbekannte Person verlangt am Telefon die Adresse Ihres Kindes. Was vermitteln Sie?",
                    options = listOf(
                        QuizOption("A", "Die Adresse nennen, wenn die Person freundlich klingt."),
                        QuizOption("B", "Nur die Schuladresse nennen."),
                        QuizOption("C", "Keine persönliche Adresse herausgeben und eine Vertrauensperson einbeziehen."),
                        QuizOption("D", "Die Adresse der Person im Gegenzug verlangen.")
                    ),
                    correctIndex = 2,
                    explanation = "Eine freundliche Stimme bedeutet nicht, dass eine Person vertrauenswürdig ist. Persönliche Daten bleiben geschützt."
                ),
                QuizQuestion(
                    id = "p_unk_3",
                    title = "Wiederholte Anrufe",
                    situation = "Eine unbekannte Nummer ruft Ihr Kind wiederholt an. Was ist sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Jedes Mal zurückrufen."),
                        QuizOption("B", "Die Nummer blockieren und bei belastenden oder verdächtigen Anrufen darüber sprechen."),
                        QuizOption("C", "Die Nummer öffentlich posten."),
                        QuizOption("D", "Die Nummer an andere Kinder weitergeben.")
                    ),
                    correctIndex = 1,
                    explanation = "Blockieren kann unerwünschte Kontakte reduzieren. Wichtig ist außerdem, dass das Kind Hilfe holen kann."
                )
            ),
            parentTips = listOf(
                "Spam-Schutz im Betriebssystem aktivieren.",
                "Kind loben, wenn es Anrufe meldet.",
                "Unbekannte Nummern gemeinsam nachschlagen."
            )
        ),

        QuizCategory.NACHRICHTEN to QuizModule(
            category = QuizCategory.NACHRICHTEN,
            title = "Nachrichten & Chats",
            childIntroText = "Nachrichten schreiben macht Spaß. Hier erfährst du, worauf du beim Schreiben achten solltest.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_msg_1",
                    title = "Antwortzeit",
                    situation = "Dein Freund schreibt dir eine Nachricht. Musst du sofort antworten?",
                    options = listOf(
                        QuizOption("A", "Ja, immer."),
                        QuizOption("B", "Nein, ich kann antworten, wenn ich Zeit habe."),
                        QuizOption("C", "Nur nachts."),
                        QuizOption("D", "Nur wenn er dreimal schreibt.")
                    ),
                    correctIndex = 1,
                    explanation = "Nachrichten müssen nicht sofort beantwortet werden. Du darfst selbst entscheiden, wann du Zeit hast."
                ),
                QuizQuestion(
                    id = "c_msg_2",
                    title = "Missverständnis",
                    situation = "Du verstehst eine Nachricht nicht. Was kannst du tun?",
                    options = listOf(
                        QuizOption("A", "Wütend antworten."),
                        QuizOption("B", "Nachfragen, was gemeint ist."),
                        QuizOption("C", "Die Person sofort blockieren."),
                        QuizOption("D", "Die Nachricht an alle weiterleiten.")
                    ),
                    correctIndex = 1,
                    explanation = "Bei Missverständnissen hilft Nachfragen. So kannst du herausfinden, was die andere Person wirklich meint."
                ),
                QuizQuestion(
                    id = "c_msg_3",
                    title = "Gemeine Nachricht",
                    situation = "Jemand schreibt dir: „Du bist doof!“ Was wäre eine gute Reaktion?",
                    options = listOf(
                        QuizOption("A", "„Du bist noch viel doofer!“"),
                        QuizOption("B", "Die Nachricht speichern und einen Erwachsenen um Hilfe bitten."),
                        QuizOption("C", "Die Nachricht an alle Freunde weiterleiten."),
                        QuizOption("D", "Sofort zurückrufen und schreien.")
                    ),
                    correctIndex = 1,
                    explanation = "Du musst nicht zurückbeleidigen. Wenn dich eine Nachricht belastet oder verletzt, kannst du Hilfe holen."
                )
            ),
            childSummaryPoints = listOf(
                "Du musst nicht sofort antworten – nimm dir Zeit.",
                "Erst nachfragen, wenn etwas unklar oder komisch klingt.",
                "Nicht zurückbeleidigen: Screenshot machen und Eltern um Hilfe bitten."
            ),
            parentIntroText = "Beim Chatten entstehen schnell Missverständnisse und Antwortdruck. Fördern Sie gelassene Kommunikation.",
            parentFocusTopics = listOf(
                "Kein permanenter Antwortdruck",
                "Konfliktlösung ohne Beleidigungen",
                "Offener Austausch bei Cybermobbing"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_msg_1",
                    title = "Antwortdruck abbauen",
                    situation = "Ihr Kind antwortet nicht sofort auf eine Nachricht. Was ist eine sinnvolle Haltung?",
                    options = listOf(
                        QuizOption("A", "Kinder müssen immer sofort antworten."),
                        QuizOption("B", "Kinder dürfen selbst entscheiden, wann sie Zeit zum Antworten haben."),
                        QuizOption("C", "Eltern sollten jede Nachricht beantworten."),
                        QuizOption("D", "Das Handy sollte deshalb weggenommen werden.")
                    ),
                    correctIndex = 1,
                    explanation = "Digitale Kommunikation bedeutet nicht, ständig erreichbar sein zu müssen."
                ),
                QuizQuestion(
                    id = "p_msg_2",
                    title = "Kontext klären",
                    situation = "Ihr Kind versteht eine Nachricht falsch und ist verärgert. Was können Sie fördern?",
                    options = listOf(
                        QuizOption("A", "Sofort zurückbeleidigen."),
                        QuizOption("B", "Nachfragen und versuchen, den Kontext zu klären."),
                        QuizOption("C", "Die Nachricht an alle weiterleiten."),
                        QuizOption("D", "Die andere Person öffentlich bloßstellen.")
                    ),
                    correctIndex = 1,
                    explanation = "Nachfragen kann Missverständnisse klären und verhindert, dass ein digitaler Konflikt unnötig eskaliert."
                ),
                QuizQuestion(
                    id = "p_msg_3",
                    title = "Beleidigungen",
                    situation = "Ihr Kind bekommt eine beleidigende Nachricht. Was ist eine sinnvolle Unterstützung?",
                    options = listOf(
                        QuizOption("A", "Zurückbeleidigen."),
                        QuizOption("B", "Die Situation gemeinsam einordnen, die Nachricht gegebenenfalls sichern und Hilfe organisieren."),
                        QuizOption("C", "Die Nachricht an alle Freunde schicken."),
                        QuizOption("D", "Die andere Person persönlich aufsuchen.")
                    ),
                    correctIndex = 1,
                    explanation = "Eltern können helfen, ruhig zu bleiben, Belege zu sichern und passende weitere Schritte zu finden."
                )
            ),
            parentTips = listOf(
                "Feste nachrichtenfreie Zeiten im Familienalltag vereinbaren.",
                "Ermutigen, unangenehme Nachrichten zu zeigen.",
                "Ruhe bewahren und Belege sichern."
            )
        ),

        QuizCategory.WHATSAPP to QuizModule(
            category = QuizCategory.WHATSAPP,
            title = "WhatsApp",
            childIntroText = "In WhatsApp kannst du mit Freunden und Gruppen schreiben. Hier lernst du die wichtigsten Regeln.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_wa_1",
                    title = "WhatsApp-Gruppe",
                    situation = "Was ist eine WhatsApp-Gruppe?",
                    options = listOf(
                        QuizOption("A", "Ein Chat mit mehreren Personen."),
                        QuizOption("B", "Ein Spiel."),
                        QuizOption("C", "Eine Telefonnummer."),
                        QuizOption("D", "Ein Fotoalbum.")
                    ),
                    correctIndex = 0,
                    explanation = "In einer Gruppe können mehrere Personen gemeinsam Nachrichten, Bilder oder andere Inhalte austauschen."
                ),
                QuizQuestion(
                    id = "c_wa_2",
                    title = "Fremde Gruppe",
                    situation = "Du wirst zu einer WhatsApp-Gruppe hinzugefügt, in der du niemanden kennst. Was machst du?",
                    options = listOf(
                        QuizOption("A", "Du erzählst sofort allen deine Adresse."),
                        QuizOption("B", "Du bleibst einfach in der Gruppe."),
                        QuizOption("C", "Du fragst einen Erwachsenen, was du tun solltest."),
                        QuizOption("D", "Du schickst allen dein Foto.")
                    ),
                    correctIndex = 2,
                    explanation = "Wenn du die Personen nicht kennst, solltest du vorsichtig sein und eine erwachsene Vertrauensperson fragen."
                ),
                QuizQuestion(
                    id = "c_wa_3",
                    title = "Gemeines Foto in Gruppe",
                    situation = "Jemand schickt in einer WhatsApp-Gruppe ein gemeines Foto von einem anderen Kind. Was solltest du tun?",
                    options = listOf(
                        QuizOption("A", "Das Foto weiterleiten."),
                        QuizOption("B", "Einen Screenshot machen und das Foto an alle schicken."),
                        QuizOption("C", "Das Foto nicht weiterverbreiten und einen Erwachsenen informieren."),
                        QuizOption("D", "Ein noch gemeineres Foto schicken.")
                    ),
                    correctIndex = 2,
                    explanation = "Ein gemeines Foto weiterzuverbreiten kann die Situation schlimmer machen. Hol dir Hilfe und verbreite es nicht weiter."
                )
            ),
            childSummaryPoints = listOf(
                "WhatsApp-Gruppen sind für mehrere Personen – alles kann von allen gelesen werden.",
                "In fremden Gruppen vorsichtig sein und sofort die Eltern fragen.",
                "Gemeine Bilder niemals weiterschicken, sondern Hilfe holen."
            ),
            parentIntroText = "WhatsApp ist oft der erste eigene Messenger. Gruppenchats bergen Dynamiken wie Kettenbriefe und Ausgrenzung.",
            parentFocusTopics = listOf(
                "Gruppen-Mitglieder kennen und prüfen",
                "Gemeine Inhalte stoppen statt teilen",
                "Verantwortung für das eigene Senden"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_wa_1",
                    title = "Unbekannte Gruppen",
                    situation = "Ihr Kind wird zu einer WhatsApp-Gruppe hinzugefügt, deren Mitglieder es nicht kennt. Was ist sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Sofort persönliche Informationen teilen."),
                        QuizOption("B", "Gemeinsam prüfen, wer die Mitglieder sind, und bei Unsicherheit Unterstützung holen."),
                        QuizOption("C", "Allen ein Foto schicken."),
                        QuizOption("D", "Die Gruppe an weitere Kinder empfehlen.")
                    ),
                    correctIndex = 1,
                    explanation = "Bei unbekannten Gruppen sollte das Kind wissen, dass es nicht teilnehmen muss und Hilfe holen kann."
                ),
                QuizQuestion(
                    id = "p_wa_2",
                    title = "Gemeines Foto in Gruppe",
                    situation = "In einer WhatsApp-Gruppe wird ein Kind mit einem peinlichen Foto verspottet. Was vermitteln Sie?",
                    options = listOf(
                        QuizOption("A", "Das Foto weiterleiten."),
                        QuizOption("B", "Das Foto nicht weiterverbreiten und Unterstützung holen."),
                        QuizOption("C", "Ein noch peinlicheres Foto zurückschicken."),
                        QuizOption("D", "Die Gruppe an weitere Personen teilen.")
                    ),
                    correctIndex = 1,
                    explanation = "Das Weiterverbreiten kann die Belastung vergrößern. Nicht weiterleiten und Hilfe holen ist sinnvoll."
                ),
                QuizQuestion(
                    id = "p_wa_3",
                    title = "Senden in große Gruppen",
                    situation = "Ihr Kind möchte etwas in eine größere Gruppe schicken. Welche Regel ist sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Alles Lustige darf verschickt werden."),
                        QuizOption("B", "Vor dem Senden überlegen, wer es sehen kann und ob es für die Gruppe geeignet ist."),
                        QuizOption("C", "Je mehr Menschen es sehen, desto besser."),
                        QuizOption("D", "Nachrichten sollten grundsätzlich öffentlich sein.")
                    ),
                    correctIndex = 1,
                    explanation = "Nicht jede Information gehört in jeden Chat. Ziel ist ein bewusster Umgang mit Empfängern und Inhalten."
                )
            ),
            parentTips = listOf(
                "Privatsphäre-Einstellungen in WhatsApp optimieren (Profilbild nur Kontakte).",
                "Regelmäßig gemeinsam über Gruppendynamiken sprechen.",
                "Ermutigen, Gruppen bei Unwohlsein zu verlassen."
            )
        ),

        QuizCategory.FOTOS_MACHEN to QuizModule(
            category = QuizCategory.FOTOS_MACHEN,
            title = "Fotos machen & Kamera",
            childIntroText = "Mit der Kamera kannst du Fotos und Videos machen. Damit kannst du schöne Momente festhalten.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_cam_1",
                    title = "Freund fotografieren",
                    situation = "Du möchtest ein Foto von deinem Freund machen. Was machst du zuerst?",
                    options = listOf(
                        QuizOption("A", "Heimlich fotografieren."),
                        QuizOption("B", "Fragen, ob du ihn fotografieren darfst."),
                        QuizOption("C", "Das Foto sofort an alle schicken."),
                        QuizOption("D", "Das Foto ins Internet stellen.")
                    ),
                    correctIndex = 1,
                    explanation = "Frage die Person vorher. Jeder Mensch darf mitentscheiden, ob ein Foto von ihm gemacht und weitergegeben wird."
                ),
                QuizQuestion(
                    id = "c_cam_2",
                    title = "Versehentliches Foto",
                    situation = "Du hast aus Versehen ein Foto gemacht, das du nicht behalten möchtest. Was kannst du machen?",
                    options = listOf(
                        QuizOption("A", "Es an alle schicken."),
                        QuizOption("B", "Es löschen."),
                        QuizOption("C", "Es posten."),
                        QuizOption("D", "Die Kamera kaputtmachen.")
                    ),
                    correctIndex = 1,
                    explanation = "Wenn du ein Foto nicht behalten möchtest, kannst du es löschen."
                ),
                QuizQuestion(
                    id = "c_cam_3",
                    title = "Hausnummer im Bild",
                    situation = "Auf einem Foto ist neben deinem Freund auch die Hausnummer zu sehen. Du möchtest das Foto verschicken. Was solltest du bedenken?",
                    options = listOf(
                        QuizOption("A", "Die Hausnummer ist egal."),
                        QuizOption("B", "Das Foto könnte persönliche Informationen zeigen."),
                        QuizOption("C", "Eine Hausnummer darf immer ins Internet."),
                        QuizOption("D", "Man sollte das Foto möglichst vielen Menschen schicken.")
                    ),
                    correctIndex = 1,
                    explanation = "Eine Hausnummer kann etwas über den Wohnort verraten. Prüfe deshalb vor dem Verschicken, welche persönlichen Informationen auf einem Foto zu sehen sind."
                )
            ),
            childSummaryPoints = listOf(
                "Andere Menschen immer fragen, bevor du sie fotografierst.",
                "Versehentliche oder unschöne Fotos einfach löschen.",
                "Auf den Hintergrund achten: Keine Hausnummern oder Adressen mitfotografieren."
            ),
            parentIntroText = "Mit der Kamera kann Ihr Kind Fotos und Videos aufnehmen. Dabei gibt es einige wichtige Themen, die Sie gemeinsam besprechen sollten.",
            parentFocusTopics = listOf(
                "Privatsphäre anderer Menschen",
                "Standortinformationen im Bild",
                "Weitergabe von Fotos",
                "Umgang mit sensiblen Inhalten"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_cam_1",
                    title = "Erlaubnis einholen",
                    situation = "Ihr Kind möchte ein anderes Kind fotografieren. Was sollten Sie vermitteln?",
                    options = listOf(
                        QuizOption("A", "Fotografieren ist immer erlaubt."),
                        QuizOption("B", "Vorher fragen, ob die Person fotografiert werden möchte."),
                        QuizOption("C", "Heimliche Fotos sind besonders lustig."),
                        QuizOption("D", "Das Foto sollte sofort veröffentlicht werden.")
                    ),
                    correctIndex = 1,
                    explanation = "Das Fragen respektiert die andere Person und schafft eine wichtige Grundlage für einen verantwortungsvollen Umgang mit Bildern."
                ),
                QuizQuestion(
                    id = "p_cam_2",
                    title = "Aussortieren und Löschen",
                    situation = "Ihr Kind hat versehentlich ein Foto gemacht, das es nicht behalten möchte. Was können Sie zeigen?",
                    options = listOf(
                        QuizOption("A", "Das Foto an andere schicken."),
                        QuizOption("B", "Das Foto löschen."),
                        QuizOption("C", "Das Foto veröffentlichen."),
                        QuizOption("D", "Die Kamera-App löschen.")
                    ),
                    correctIndex = 1,
                    explanation = "Ein Foto muss nicht behalten werden. Das Kind kann lernen, nicht benötigte Aufnahmen zu löschen."
                ),
                QuizQuestion(
                    id = "p_cam_3",
                    title = "Hintergrunddetails",
                    situation = "Auf einem Foto sind eine Hausnummer und weitere persönliche Informationen zu erkennen. Was sollten Sie besprechen?",
                    options = listOf(
                        QuizOption("A", "Solche Informationen sind egal."),
                        QuizOption("B", "Vor dem Teilen prüfen, welche persönlichen Informationen das Foto verrät."),
                        QuizOption("C", "Das Foto möglichst öffentlich posten."),
                        QuizOption("D", "Die Hausnummer in den Beitrag schreiben.")
                    ),
                    correctIndex = 1,
                    explanation = "Fotos können neben dem eigentlichen Motiv weitere Informationen über Personen und Orte enthalten."
                )
            ),
            parentTips = listOf(
                "Standortinformationen bei Fotos prüfen (Geotagging).",
                "Regeln zur Weitergabe von Familienbildern besprechen.",
                "Sensibler Umgang mit Fotos fremder Personen."
            )
        ),

        QuizCategory.FOTOS_VON_ANDEREN to QuizModule(
            category = QuizCategory.FOTOS_VON_ANDEREN,
            title = "Fotos von anderen",
            childIntroText = "Bilder von anderen Menschen sind privat. Lerne, wie du respektvoll damit umgehst.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_oth_1",
                    title = "Lustiges Foto teilen",
                    situation = "Du hast ein lustiges Foto von deinem Freund gemacht. Darfst du es einfach in eine WhatsApp-Gruppe schicken?",
                    options = listOf(
                        QuizOption("A", "Ja, immer."),
                        QuizOption("B", "Nur wenn niemand das Foto sehen kann."),
                        QuizOption("C", "Du solltest zuerst fragen, ob das okay ist."),
                        QuizOption("D", "Ja, wenn das Foto lustig ist.")
                    ),
                    correctIndex = 2,
                    explanation = "Auch ein lustiges Foto sollte nicht einfach ohne Zustimmung weitergegeben werden."
                ),
                QuizQuestion(
                    id = "c_oth_2",
                    title = "Bitte nicht weiterschicken",
                    situation = "Deine Freundin sagt: „Bitte schick dieses Foto nicht weiter.“ Was machst du?",
                    options = listOf(
                        QuizOption("A", "Trotzdem weiterschicken."),
                        QuizOption("B", "Nur an eine Person schicken."),
                        QuizOption("C", "Das Foto nicht weitergeben."),
                        QuizOption("D", "Es ins Internet stellen.")
                    ),
                    correctIndex = 2,
                    explanation = "Wenn jemand ausdrücklich sagt, dass ein Foto nicht weitergegeben werden soll, respektierst du diesen Wunsch."
                )
            ),
            childSummaryPoints = listOf(
                "Auch lustige Bilder nicht ohne Zustimmung verschicken.",
                "Wenn jemand 'Nein' sagt: Wunsch immer respektieren.",
                "Das Recht am eigenen Bild gilt für alle."
            ),
            parentIntroText = "Das Recht am eigenen Bild ist eine zentrale Medienkompetenz für Kinder und Jugendliche.",
            parentFocusTopics = listOf(
                "Recht am eigenen Bild verstehen",
                "Respekt vor den Grenzen anderer",
                "Gemeinsames Abwägen vor Veröffentlichungen"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_oth_1",
                    title = "Bilder in Gruppen teilen",
                    situation = "Ihr Kind möchte ein lustiges Foto von einem Freund in eine WhatsApp-Gruppe schicken. Was ist sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Lustige Fotos dürfen immer weitergegeben werden."),
                        QuizOption("B", "Prüfen, ob die abgebildete Person mit dem Teilen einverstanden ist."),
                        QuizOption("C", "In Gruppen gelten keine Regeln."),
                        QuizOption("D", "Es reicht, wenn mindestens fünf Personen das Foto lustig finden.")
                    ),
                    correctIndex = 1,
                    explanation = "Auch in privaten Gruppen sollte der Umgang mit Bildern anderer Menschen respektvoll sein."
                ),
                QuizQuestion(
                    id = "p_oth_2",
                    title = "Grenzen respektieren",
                    situation = "Ein anderes Kind bittet darum, ein Foto nicht weiterzugeben. Was sollte Ihr Kind tun?",
                    options = listOf(
                        QuizOption("A", "Die Bitte ignorieren."),
                        QuizOption("B", "Das Foto nicht weitergeben."),
                        QuizOption("C", "Das Foto an eine Person schicken."),
                        QuizOption("D", "Das Foto öffentlich posten.")
                    ),
                    correctIndex = 1,
                    explanation = "Die ausdrückliche Bitte, ein Foto nicht weiterzugeben, sollte respektiert werden."
                ),
                QuizQuestion(
                    id = "p_oth_3",
                    title = "Gruppenfotos veröffentlichen",
                    situation = "Ihr Kind möchte ein Foto veröffentlichen, auf dem mehrere Kinder zu sehen sind. Was ist sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Sofort veröffentlichen."),
                        QuizOption("B", "Gemeinsam prüfen, ob die Veröffentlichung für die abgebildeten Personen in Ordnung ist."),
                        QuizOption("C", "Nur den Beitragstext ändern."),
                        QuizOption("D", "Das Foto zusätzlich an unbekannte Personen schicken.")
                    ),
                    correctIndex = 1,
                    explanation = "Bei Bildern mit anderen Personen sollte auch deren Situation und Privatsphäre berücksichtigt werden."
                )
            ),
            parentTips = listOf(
                "Vorbild sein: Selbst nicht ungefragt Bilder der Kinder posten.",
                "Mit dem Kind die Gefühle der abgebildeten Person besprechen.",
                "Eindeutige Familienregeln für das Teilen von Fotos vereinbaren."
            )
        ),

        QuizCategory.SPRACHNACHRICHTEN to QuizModule(
            category = QuizCategory.SPRACHNACHRICHTEN,
            title = "Sprachnachrichten",
            childIntroText = "Sprachnachrichten sind praktisch, wenn man nicht tippen möchte.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_vn_1",
                    title = "Sprachnachricht empfangen",
                    situation = "Du bekommst eine Sprachnachricht. Was kannst du machen?",
                    options = listOf(
                        QuizOption("A", "Du kannst sie anhören, wenn du Zeit hast."),
                        QuizOption("B", "Du musst sofort antworten."),
                        QuizOption("C", "Du musst sie an alle weiterleiten."),
                        QuizOption("D", "Du musst sofort anrufen.")
                    ),
                    correctIndex = 0,
                    explanation = "Du darfst selbst entscheiden, wann du eine Sprachnachricht anhörst und beantwortest."
                ),
                QuizQuestion(
                    id = "c_vn_2",
                    title = "Aufnahme in der Umgebung",
                    situation = "Du möchtest eine Sprachnachricht aufnehmen. Neben dir sitzen viele andere Kinder. Was solltest du bedenken?",
                    options = listOf(
                        QuizOption("A", "Laut hineinrufen."),
                        QuizOption("B", "Darauf achten, dass andere nicht unfreiwillig mit aufgenommen werden."),
                        QuizOption("C", "Alle Kinder aufnehmen."),
                        QuizOption("D", "Die Nachricht möglichst laut abspielen.")
                    ),
                    correctIndex = 1,
                    explanation = "Achte auf die Privatsphäre anderer. Andere Personen sollten nicht einfach Teil deiner Aufnahme werden."
                )
            ),
            childSummaryPoints = listOf(
                "Du bestimmst, wann du Sprachnachrichten abhörst.",
                "Beim Aufnehmen darauf achten, wer in der Nähe ist.",
                "Nicht laut im Bus oder in der Öffentlichkeit abhören."
            ),
            parentIntroText = "Sprachnachrichten sind bei Kindern sehr beliebt, können aber die Privatsphäre von Umstehenden verletzen.",
            parentFocusTopics = listOf(
                "Privatsphäre bei Umgebungsgeräuschen",
                "Kopfhörer nutzen im öffentlichen Raum",
                "Angemessene Länge und Höflichkeit"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_vn_1",
                    title = "Keine Sofortpflicht",
                    situation = "Ihr Kind bekommt eine Sprachnachricht und möchte erst später zuhören. Was ist sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Es muss sofort antworten."),
                        QuizOption("B", "Es darf die Nachricht anhören, wenn es Zeit und Ruhe dafür hat."),
                        QuizOption("C", "Es muss die Nachricht weiterleiten."),
                        QuizOption("D", "Es muss sofort zurückrufen.")
                    ),
                    correctIndex = 1,
                    explanation = "Auch Sprachnachrichten erzeugen keine Pflicht, sofort zu reagieren."
                ),
                QuizQuestion(
                    id = "p_vn_2",
                    title = "Aufnahme in Gegenwart anderer",
                    situation = "Ihr Kind nimmt eine Sprachnachricht auf, während andere Kinder daneben sitzen. Was sollte es beachten?",
                    options = listOf(
                        QuizOption("A", "Andere dürfen automatisch mit aufgenommen werden."),
                        QuizOption("B", "Darauf achten, dass andere nicht unbeabsichtigt Teil der Aufnahme werden."),
                        QuizOption("C", "Möglichst laut sprechen."),
                        QuizOption("D", "Die Aufnahme öffentlich teilen.")
                    ),
                    correctIndex = 1,
                    explanation = "Bei Audioaufnahmen sollte die Privatsphäre anderer berücksichtigt werden."
                )
            ),
            parentTips = listOf(
                "Kind anregen, Kopfhörer für Sprachnachrichten zu nutzen.",
                "Darauf hinweisen, dass auch Gesprochenes gespeichert bleibt.",
                "Höflicher Umgangston gilt auch für gesprochene Nachrichten."
            )
        ),

        QuizCategory.BLOCKIEREN to QuizModule(
            category = QuizCategory.BLOCKIEREN,
            title = "Blockieren & Schutz",
            childIntroText = "Wenn dich jemand nervt oder ärgert, kannst du die Person blockieren.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_blk_1",
                    title = "Unerwünschte Nachrichten",
                    situation = "Jemand schreibt dir immer wieder Nachrichten, obwohl du das nicht möchtest. Was kannst du machen?",
                    options = listOf(
                        QuizOption("A", "Die Person ebenfalls beleidigen."),
                        QuizOption("B", "Die Person blockieren und Hilfe holen."),
                        QuizOption("C", "Deine Adresse schicken."),
                        QuizOption("D", "Die Nachrichten an alle weiterleiten.")
                    ),
                    correctIndex = 1,
                    explanation = "Du musst unerwünschte Nachrichten nicht aushalten. Blockieren und Hilfe holen sind mögliche Schritte."
                ),
                QuizQuestion(
                    id = "c_blk_2",
                    title = "Was bewirkt Blockieren?",
                    situation = "Was passiert grundsätzlich, wenn du jemanden bei WhatsApp blockierst?",
                    options = listOf(
                        QuizOption("A", "Die Person bekommt automatisch dein Passwort."),
                        QuizOption("B", "Die Person kann dir über WhatsApp nicht mehr normal schreiben oder dich dort anrufen."),
                        QuizOption("C", "Dein Handy geht aus."),
                        QuizOption("D", "Alle deine Fotos werden gelöscht.")
                    ),
                    correctIndex = 1,
                    explanation = "Durch das Blockieren wird die direkte Kommunikation über WhatsApp eingeschränkt. Dein Handy oder deine Fotos werden dadurch nicht gelöscht."
                )
            ),
            childSummaryPoints = listOf(
                "Du musst keine gemeinen Nachrichten ertragen.",
                "Blockieren schützt dich vor Anrufen und Texten der Person.",
                "Immer mit einem Erwachsenen sprechen, wenn etwas unangenehm ist."
            ),
            parentIntroText = "Die Blockieren-Funktion ist ein wirksames Werkzeug für Kinder, um Grenzen im digitalen Raum zu setzen.",
            parentFocusTopics = listOf(
                "Blockieren als Schutzmechanismus",
                "Situation gemeinsam analysieren",
                "Keine Angst vor Konsequenzen machen"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_blk_1",
                    title = "Unerwünschte Nachrichten begleiten",
                    situation = "Ihr Kind bekommt wiederholt unerwünschte Nachrichten. Was können Sie vermitteln?",
                    options = listOf(
                        QuizOption("A", "Zurückbeleidigen."),
                        QuizOption("B", "Die Person gegebenenfalls blockieren und gemeinsam überlegen, ob weitere Hilfe nötig ist."),
                        QuizOption("C", "Die Nachrichten weiterleiten."),
                        QuizOption("D", "Die eigene Adresse schicken.")
                    ),
                    correctIndex = 1,
                    explanation = "Blockieren kann die direkte Kommunikation unterbrechen. Bei belastenden Situationen sollte gemeinsam über weitere Hilfe gesprochen werden."
                ),
                QuizQuestion(
                    id = "p_blk_2",
                    title = "Funktion erklären",
                    situation = "Ihr Kind möchte eine Person blockieren, weil diese wiederholt unangenehme Nachrichten schickt. Wie reagieren Sie sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Kinder dürfen niemals blockieren."),
                        QuizOption("B", "Gemeinsam die Situation anschauen und erklären, wie die Funktion funktioniert."),
                        QuizOption("C", "Die Nachrichten an alle Freunde schicken."),
                        QuizOption("D", "Das Kind soll die Person zuerst provozieren.")
                    ),
                    correctIndex = 1,
                    explanation = "Eltern können das Kind unterstützen, eine passende Grenze zu setzen und die Funktion bewusst zu nutzen."
                )
            ),
            parentTips = listOf(
                "Kind zeigen, wie Blockieren in verschiedenen Apps funktioniert.",
                "Signalisieren, dass das Kind bei Problemen nicht bestraft wird.",
                "Bei schwerwiegendem Cybermobbing Beweise sichern und Schule einbinden."
            )
        ),

        QuizCategory.PERSOENLICHE_DATEN to QuizModule(
            category = QuizCategory.PERSOENLICHE_DATEN,
            title = "Persönliche Daten",
            childIntroText = "Deine persönlichen Daten wie Adresse und Schule gehören nur dir.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_dat_1",
                    title = "Schützenswerte Informationen",
                    situation = "Welche Information solltest du einer unbekannten Person im Internet nicht einfach geben?",
                    options = listOf(
                        QuizOption("A", "Deine Lieblingsfarbe."),
                        QuizOption("B", "Deine Adresse."),
                        QuizOption("C", "Dein Lieblingsessen."),
                        QuizOption("D", "Deinen Lieblingssport.")
                    ),
                    correctIndex = 1,
                    explanation = "Deine Adresse gehört zu deinen persönlichen Informationen und sollte nicht einfach an fremde Personen gegeben werden."
                ),
                QuizQuestion(
                    id = "c_dat_2",
                    title = "Ausfrageversuch",
                    situation = "Jemand schreibt dir: „Wie heißt du, wo wohnst du und auf welche Schule gehst du?“ Was machst du?",
                    options = listOf(
                        QuizOption("A", "Ich beantworte alles."),
                        QuizOption("B", "Ich schicke ein Foto von meinem Ausweis."),
                        QuizOption("C", "Ich gebe die Informationen nicht heraus und frage einen Erwachsenen."),
                        QuizOption("D", "Ich schicke meine Adresse, aber nicht meinen Namen.")
                    ),
                    correctIndex = 2,
                    explanation = "Wenn eine unbekannte Person viele persönliche Informationen möchte, gib sie nicht heraus und hole dir Unterstützung."
                )
            ),
            childSummaryPoints = listOf(
                "Adresse, Nachname und Schule sind geheim.",
                "Fremden im Netz niemals Fotos oder Standortdaten geben.",
                "Wenn jemand viele Fragen stellt: Sofort Mama oder Papa holen."
            ),
            parentIntroText = "Datenschutz fängt im Kindesalter an. Vermitteln Sie, warum private Daten im Netz geschützt werden müssen.",
            parentFocusTopics = listOf(
                "Wohnort und Schule schützen",
                "Vertrauensvolle Ansprechperson sein",
                "Sensibilisierung für Social Engineering"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_dat_1",
                    title = "Besonders schützenswerte Daten",
                    situation = "Welche Information sollten Eltern als besonders schützenswert erklären?",
                    options = listOf(
                        QuizOption("A", "Lieblingsfarbe."),
                        QuizOption("B", "Wohnadresse."),
                        QuizOption("C", "Lieblingsessen."),
                        QuizOption("D", "Lieblingssport.")
                    ),
                    correctIndex = 1,
                    explanation = "Eine Wohnadresse ist eine persönliche Information und sollte nicht einfach an unbekannte Personen weitergegeben werden."
                ),
                QuizQuestion(
                    id = "p_dat_2",
                    title = "Ausfrage-Situationen",
                    situation = "Eine unbekannte Person fragt Ihr Kind nach Name, Schule und Wohnort. Was ist eine gute Regel?",
                    options = listOf(
                        QuizOption("A", "Alles angeben, wenn die Person freundlich ist."),
                        QuizOption("B", "Keine persönlichen Angaben herausgeben und eine erwachsene Vertrauensperson einbeziehen."),
                        QuizOption("C", "Nur den Wohnort nennen."),
                        QuizOption("D", "Ein Foto des Ausweises schicken.")
                    ),
                    correctIndex = 1,
                    explanation = "Unbekannte Personen müssen nicht wissen, wo ein Kind wohnt oder zur Schule geht."
                ),
                QuizQuestion(
                    id = "p_dat_3",
                    title = "Hilfreiche Haltung",
                    situation = "Welche Haltung ist beim Datenschutz für Kinder besonders hilfreich?",
                    options = listOf(
                        QuizOption("A", "Kinder müssen alles allein wissen."),
                        QuizOption("B", "Kinder sollen wissen, dass sie bei Unsicherheit jederzeit eine Vertrauensperson fragen können."),
                        QuizOption("C", "Persönliche Daten sind im Internet egal."),
                        QuizOption("D", "Datenschutz ist nur für Erwachsene wichtig.")
                    ),
                    correctIndex = 1,
                    explanation = "Eine verlässliche Ansprechperson gibt Kindern Sicherheit, wenn sie eine Situation nicht einschätzen können."
                )
            ),
            parentTips = listOf(
                "Keine Klarnamen und Geburtsdaten in Profilen eintragen.",
                "Regelmäßig prüfen, welche Daten Apps abfragen.",
                "Echtes Vertrauen aufbauen, damit das Kind Unsicherheiten anspricht."
            )
        ),

        QuizCategory.BEVOR_VERSCHICKEN to QuizModule(
            category = QuizCategory.BEVOR_VERSCHICKEN,
            title = "Bevor ich etwas verschicke",
            childIntroText = "Bevor du auf 'Senden' tippst, denke kurz nach. Ein verschicktes Foto kann man nicht einfach zurückholen.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_snd_1",
                    title = "Kurzer Check vor dem Senden",
                    situation = "Du möchtest ein Foto verschicken. Was solltest du vorher machen?",
                    options = listOf(
                        QuizOption("A", "Sofort auf „Senden“ drücken."),
                        QuizOption("B", "Prüfen, ob das richtige Foto ausgewählt ist und ob du es verschicken darfst."),
                        QuizOption("C", "Das Foto zuerst an zehn Personen schicken."),
                        QuizOption("D", "Das Handy weglegen und nichts mehr machen.")
                    ),
                    correctIndex = 1,
                    explanation = "Schau vor dem Senden noch einmal genau hin: Ist es das richtige Foto und ist es in Ordnung, es an diese Person zu schicken?"
                ),
                QuizQuestion(
                    id = "c_snd_2",
                    title = "Lustiges Bild weiterleiten",
                    situation = "Du bekommst ein lustiges Bild und möchtest es an deine Freunde weiterleiten. Was solltest du überlegen?",
                    options = listOf(
                        QuizOption("A", "Ist das Bild für andere vielleicht unangenehm oder privat?"),
                        QuizOption("B", "Wie schnell kann ich es verschicken?"),
                        QuizOption("C", "Wie viele Menschen kann ich damit erreichen?"),
                        QuizOption("D", "Ob ich noch mehr Bilder finde.")
                    ),
                    correctIndex = 0,
                    explanation = "Vor dem Weiterleiten solltest du überlegen, ob das Bild privat, unangenehm oder verletzend sein könnte."
                )
            ),
            childSummaryPoints = listOf(
                "Einmal verschickt, ist ein Bild im Netz kaum wieder löschbar.",
                "Kurz überlegen: Ist das Bild für andere peinlich oder verletzend?",
                "Bei Unsicherheit lieber einmal mehr die Eltern fragen."
            ),
            parentIntroText = "Die 'Denk-Pause' vor dem Absenden ist eine der wichtigsten Schutzgewohnheiten in der Mediennutzung.",
            parentFocusTopics = listOf(
                "Impulsives Senden vermeiden",
                "Empathie für den Empfänger stärken",
                "Unterstützung bei der Bildauswahl"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_snd_1",
                    title = "Check vor dem Senden",
                    situation = "Ihr Kind möchte ein Foto verschicken. Welche Regel ist sinnvoll?",
                    options = listOf(
                        QuizOption("A", "Möglichst schnell auf Senden drücken."),
                        QuizOption("B", "Prüfen, ob es das richtige Foto ist und ob es an diese Person geschickt werden darf."),
                        QuizOption("C", "Das Foto zuerst an viele Personen schicken."),
                        QuizOption("D", "Fotos grundsätzlich öffentlich teilen.")
                    ),
                    correctIndex = 1,
                    explanation = "Ein kurzer Check vor dem Senden kann verhindern, dass ein falsches oder ungeeignetes Bild weitergegeben wird."
                ),
                QuizQuestion(
                    id = "p_snd_2",
                    title = "Empathie bei Humor",
                    situation = "Ihr Kind möchte ein lustiges Bild weiterleiten. Was sollten Sie fördern?",
                    options = listOf(
                        QuizOption("A", "Möglichst viele Menschen erreichen."),
                        QuizOption("B", "Prüfen, ob das Bild privat, unangenehm oder verletzend sein könnte."),
                        QuizOption("C", "Bilder immer weiterleiten, wenn sie lustig sind."),
                        QuizOption("D", "Nur die Dateigröße prüfen.")
                    ),
                    correctIndex = 1,
                    explanation = "Humor ist nicht für alle gleich. Vor dem Weiterleiten sollte geprüft werden, ob andere dadurch verletzt oder bloßgestellt werden."
                ),
                QuizQuestion(
                    id = "p_snd_3",
                    title = "Gemeinsam entscheiden",
                    situation = "Ihr Kind fragt: „Darf ich dieses Foto schicken?“ Was ist eine gute Elternreaktion?",
                    options = listOf(
                        QuizOption("A", "Immer sofort Ja sagen."),
                        QuizOption("B", "Gemeinsam anschauen, an wen es gehen soll, wer darauf zu sehen ist und welche Informationen das Bild enthält."),
                        QuizOption("C", "Immer Nein sagen."),
                        QuizOption("D", "Nur prüfen, ob das Foto scharf ist.")
                    ),
                    correctIndex = 1,
                    explanation = "Gemeinsames Prüfen stärkt die Medienkompetenz und hilft dem Kind, eigene Entscheidungen zunehmend selbst zu treffen."
                )
            ),
            parentTips = listOf(
                "Gemeinsam ein Stopp-Signal vor dem Absenden üben.",
                "Echtes Interesse an den Lieblings-Memes des Kindes zeigen.",
                "Offen über die Gefühle anderer sprechen."
            )
        ),

        QuizCategory.INTERNET_SURFEN to QuizModule(
            category = QuizCategory.INTERNET_SURFEN,
            title = "Sicheres Surfen & Internet",
            childIntroText = "Im Internet gibt es tolle Seiten zum Lernen, Spielen und Entdecken. Hier lernst du wichtige Regeln für sicheres Surfen.",
            childQuestions = listOf(
                QuizQuestion(
                    id = "c_surf_1",
                    title = "Unheimliche oder gruselige Inhalte",
                    situation = "Du tippst auf eine Seite und plötzlich siehst du ein Bild oder Video, das dir Angst macht oder eklig ist. Was machst du?",
                    options = listOf(
                        QuizOption("A", "Du drückst auf Weiter und schaust es dir ganz genau an."),
                        QuizOption("B", "Du schließt sofort den Browser oder legst das Handy hin und sagst deinen Eltern Bescheid."),
                        QuizOption("C", "Du schickst den Link an deine Schulfreunde."),
                        QuizOption("D", "Du versteckst dein Handy unter dem Kissen und sagst niemandem etwas.")
                    ),
                    correctIndex = 1,
                    explanation = "Wenn dir etwas im Internet Angst macht oder unangenehm ist: Sofort wegklicken und deinen Eltern Bescheid sagen. Du bekommst dafür niemals Ärger!"
                ),
                QuizQuestion(
                    id = "c_surf_2",
                    title = "Geheimnisse & persönliche Daten",
                    situation = "Auf einer Webseite steht: 'Gib deinen Namen, deine Telefonnummer und deine Adresse ein, um tolle Sticker zu gewinnen!' Was tust du?",
                    options = listOf(
                        QuizOption("A", "Du tippst sofort deine echte Adresse und Telefonnummer ein."),
                        QuizOption("B", "Du gibst die Adresse von deinem Nachbarn ein."),
                        QuizOption("C", "Du fragst zuerst deine Eltern, bevor du irgendwo deinen Namen oder deine Adresse eingibst."),
                        QuizOption("D", "Du tippst das Passwort deiner Eltern ein.")
                    ),
                    correctIndex = 2,
                    explanation = "Niemals deinen echten Namen, deine Adresse oder Telefonnummer im Internet eingeben, ohne dass deine Eltern es ausdrücklich erlauben."
                ),
                QuizQuestion(
                    id = "c_surf_3",
                    title = "Werbung & Gewinnspiele",
                    situation = "Plötzlich blinkt ein großes rotes Fenster: 'Herzlichen Glückwunsch! Du hast ein neues Smartphone gewonnen! Klicke hier!' Was bedeutet das?",
                    options = listOf(
                        QuizOption("A", "Das ist eine Werbefalle – am besten das Fenster schließen und nicht draufklicken."),
                        QuizOption("B", "Ein echtes Geschenk, du musst so schnell wie möglich draufklicken."),
                        QuizOption("C", "Eine wichtige Nachricht von deiner Schule."),
                        QuizOption("D", "Das Handy schenkt dir etwas Schönes.")
                    ),
                    correctIndex = 0,
                    explanation = "Im Internet verschenkt niemand einfach so teure Geräte. Solche bunten, blinkenden Pop-ups sind fast immer Werbung oder Abofallen."
                ),
                QuizQuestion(
                    id = "c_surf_4",
                    title = "Nachrichten von Fremden",
                    situation = "In einem Online-Spiel oder auf einer Kinderseite schreibt dir jemand: 'Wie heißt du wirklich und wo wohnst du? Wollen wir uns treffen?' Wie reagierst du?",
                    options = listOf(
                        QuizOption("A", "Du sagst deinen Wohnort und verabredest dich heimlich."),
                        QuizOption("B", "Du antwortest gar nicht, verrätst nichts über dich und zeigst die Nachricht sofort deinen Eltern."),
                        QuizOption("C", "Du schickst ein Foto von deinem Kinderzimmer."),
                        QuizOption("D", "Du lädst die Person zu dir nach Hause ein.")
                    ),
                    correctIndex = 1,
                    explanation = "Im Internet weiß man nie, wer wirklich am anderen Ende sitzt. Verrate niemals private Infos und sag immer sofort deinen Eltern Bescheid."
                )
            ),
            childSummaryPoints = listOf(
                "Bei komischen oder gruseligen Inhalten: Browser schließen und direkt den Eltern Bescheid sagen.",
                "Echte Namen, Adressen und Telefonnummern bleiben privat und geheim.",
                "Blinkende Gewinnspiele ('Du hast gewonnen!') sind Werbung und niemals anzuklicken.",
                "Niemals mit fremden Personen im Internet verabreden oder private Geheimnisse teilen."
            ),
            parentIntroText = "Das Internet bietet enorme Chancen, erfordert aber bei Kindern klare Schutzräume, Vereinbarungen und eine vertrauensvolle Begleitung.",
            parentFocusTopics = listOf(
                "Geschützte Einstiegsräume (Walled Garden) nutzen",
                "Vertrauensvolle Fehlerkultur etablieren",
                "Feste Surf-Zeiten und klare Absprachen",
                "Quellenkritik und Werbekompetenz fördern"
            ),
            parentQuestions = listOf(
                QuizQuestion(
                    id = "p_surf_1",
                    title = "Sicherer Einstieg & Walled Garden",
                    situation = "Ihr Kind unternimmt die ersten Schritte im Internet. Was ist medienpädagogisch die empfehlenswerteste Methode?",
                    options = listOf(
                        QuizOption("A", "Ein geschützter Raum ('Walled Garden') mit vorab freigegebenen Kinderseiten und Whitelisting."),
                        QuizOption("B", "Dem Kind direkt unbeschränkten Zugriff auf globale Suchmaschinen und soziale Netzwerke geben."),
                        QuizOption("C", "Das Internet bis zum 18. Lebensjahr komplett verbieten."),
                        QuizOption("D", "Das Kind ohne vorherige Absprachen allein im offenen Netz surfen lassen.")
                    ),
                    correctIndex = 0,
                    explanation = "Medienexperten (klicksafe, Schau Hin!) empfehlen für Grundschulkinder geprüfte Whitelists und kindgerechte Portale, um Reizüberflutung und ungeeignete Inhalte zu vermeiden."
                ),
                QuizQuestion(
                    id = "p_surf_2",
                    title = "Reaktion bei verstörenden Inhalten",
                    situation = "Ihr Kind kommt zu Ihnen und erzählt aufgelöst, dass es beim Surfen ein unheimliches oder verstörendes Bild gesehen hat. Wie reagieren Sie am besten?",
                    options = listOf(
                        QuizOption("A", "Mit Schimpfen und sofortigem Handy-Entzug, damit das Kind lernt, vorsichtiger zu sein."),
                        QuizOption("B", "Ruhig bleiben, das Kind dafür loben, dass es Vertrauen hat und Bescheid gesagt hat, und gemeinsam darüber sprechen."),
                        QuizOption("C", "Das Thema ignorieren und so tun, als wäre nichts passiert."),
                        QuizOption("D", "Das Gerät sofort wegsperren und dem Kind Vorwürfe machen.")
                    ),
                    correctIndex = 1,
                    explanation = "Bestrafung führt dazu, dass Kinder künftige Vorfälle verheimlichen. Lob für das Anvertrauen stärkt das Vertrauensverhältnis nachhaltig."
                ),
                QuizQuestion(
                    id = "p_surf_3",
                    title = "Feste Zeitfenster & Absprachen",
                    situation = "Wie unterstützen Sie Ihr Kind dabei, beim Surfen nicht das Zeitgefühl zu verlieren?",
                    options = listOf(
                        QuizOption("A", "Das Kind unbegrenzt surfen lassen, bis der Akku leer ist."),
                        QuizOption("B", "Vorab feste, überschaubare Zeiten (z. B. 20–30 Minuten) und ein konkretes Ziel vereinbaren und einen Wecker nutzen."),
                        QuizOption("C", "Jede Minute des Surfens per Videoüberwachung filmen."),
                        QuizOption("D", "Nur nachts surfen erlauben.")
                    ),
                    correctIndex = 1,
                    explanation = "Klare, vorab vereinbarte Zeitbudgets mit akustischem Signal (z. B. Wecker) helfen Kindern, ein eigenes Zeitgefühl zu entwickeln, ohne ständigen Streit."
                ),
                QuizQuestion(
                    id = "p_surf_4",
                    title = "Kritisches Hinterfragen von Inhalten",
                    situation = "Ihr Kind stößt auf eine Webseite mit reißerischer Überschrift. Welche Kompetenz sollten Sie fördern?",
                    options = listOf(
                        QuizOption("A", "Alles bedingungslos glauben, was im Internet steht."),
                        QuizOption("B", "Gemeinsam hinterfragen: Wer hat das geschrieben? Will die Seite etwas verkaufen oder informieren?"),
                        QuizOption("C", "Sofort alle Texte ausdrucken."),
                        QuizOption("D", "Webseiten nur nach den buntesten Farben bewerten.")
                    ),
                    correctIndex = 1,
                    explanation = "Quellenkritik ist eine Kernkompetenz: Kinder lernen am besten im Dialog, Inhalte und Werbung kritisch zu hinterfragen."
                )
            ),
            parentTips = listOf(
                "Gemeinsam surfen: Begleiten Sie Ihr Kind besonders anfangs aktiv bei Entdeckungsreisen im Web.",
                "Fehlerkultur etablieren: Betonen Sie regelmäßig, dass Ihr Kind nie Ärger bekommt, wenn es auf eine unangenehme Seite stößt und Bescheid sagt.",
                "Kindersuchmaschinen nutzen: Zeigen Sie Seiten wie fragfinn.de, blindekuh.de oder die KiKa-Mediathek."
            )
        )
    )

    fun getModule(category: QuizCategory): QuizModule {
        return allModules[category] ?: allModules.values.first()
    }
}
