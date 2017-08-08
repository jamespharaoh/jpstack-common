module Main where

import qualified Data.Text.Lazy.IO as TextIo

import qualified Text.Blaze.Html.Renderer.Text as HtmlRenderer

import qualified Wbs.TagLang as TagLang
import qualified Wbs.Docs.Data as Data
import qualified Wbs.Docs.Render as Render

main :: IO ()
main = do

	-- read input
	putStrLn "Loading data"
	inputTagsTemp <- TagLang.parseFile "input"
	let inputTags = case inputTagsTemp of
		Left error -> fail $ show error
		Right value -> value
	print inputTags

	-- convert to elems
	let inputElems = case mapM Data.tagToElem inputTags of
		Left error -> fail $ show error
		Right value -> value
	print inputElems

	-- render page to abstract form
	putStrLn "Generating page"
	let abstractPage = Render.renderPage inputElems

	-- render page to concrete form
	putStrLn "Rendering HTML"
	let concretePage = HtmlRenderer.renderHtml abstractPage

	-- write page to file
	putStrLn "Writing output"
	TextIo.writeFile "output.html" concretePage

	putStrLn "All done"
